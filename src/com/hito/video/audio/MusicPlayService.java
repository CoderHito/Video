package com.hito.video.audio;

import java.io.IOException;
import java.util.ArrayList;

import com.hito.video.R;
import com.hito.video.video.domain.AudioItem;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.widget.Toast;

public class MusicPlayService extends Service {

	/**
	 * ��Ƶ����׼����ɵ�ʱ���͵���Ϣ
	 */
	protected static final String PREPARED_MSG = "com.hito.musicplayer.prepared";
	private ArrayList<AudioItem> audioItems;
	private SharedPreferences sp;
	/**
	 * ��ǰ��Ƶ��Ϣ
	 */
	private AudioItem currentAudioItem;
	/**
	 * ��ǰ��Ƶλ��
	 */
	private int currentPosition;
	/**
	 * ý�岥����,���Բ��ű�����Ƶ���������֣�������Ƶ����������
	 */
	private MediaPlayer mediaPlayer;

	/**
	 * Ĭ��ģʽ --˳��ѭ��
	 */
	public static int REPRAT_MODE_NORMAL = 0;
	/**
	 * ����ѭ��
	 */
	public static int REPRAT_MODE_CURRENT = 1;
	/**
	 * ����ȫ��
	 */
	public static int REPRAT_MODE_ALL = 2;

	/**
	 * ����ģʽ
	 */
	private int playmodel = REPRAT_MODE_NORMAL;
	/**
	 * �Ƿ񲥷����
	 */
	private boolean isCompletion = false;

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			isCompletion = false;
			notifyChange(PREPARED_MSG);
			play();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return iBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initData();
	}

	/**
	 * �������ݣ�������һ�εĲ���ģʽ
	 */
	private void initData() {
		sp = getSharedPreferences("config", MODE_PRIVATE);
		playmodel = sp.getInt("platmode", REPRAT_MODE_NORMAL);
		getAllAudio();

	}

	/**
	 * ���͹㲥
	 * 
	 * @param msg
	 */
	protected void notifyChange(String msg) {
		Intent intent = new Intent();
		intent.setAction(msg);
		sendBroadcast(intent);
	}

	
	private OnCompletionListener mOnCompletionListene = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			isCompletion = true;
			playNext();
		}
	};
	private OnErrorListener mOnErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Toast.makeText(getApplicationContext(), "���ų�����..", 0).show();
			return true;
		}
	};
	private IMusicPlayService.Stub iBinder = new IMusicPlayService.Stub() {
		MusicPlayService service = MusicPlayService.this;

		@Override
		public void openAudio(int positon) throws RemoteException {
			try {
				service.openAudio(positon);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void play() throws RemoteException {
			service.play();
		}

		@Override
		public void setPlayModel(int model) throws RemoteException {
			service.setPlayModel(model);
		}

		@Override
		public void seekTo(int position) throws RemoteException {
			service.seekTo(position);
		}

		@Override
		public void playPre() throws RemoteException {
			service.playPre();
		}

		@Override
		public void playNext() throws RemoteException {
			service.playNext();
		}

		@Override
		public void pause() throws RemoteException {
			service.pause();
		}

		@Override
		public String getName() throws RemoteException {
			return service.getName();
		}

		@Override
		public int getDuration() throws RemoteException {
			return service.getDuration();
		}

		@Override
		public int getCurrentPosition() throws RemoteException {
			return service.getCurrentPosition();
		}

		@Override
		public String getArtist() throws RemoteException {
			return service.getArtist();
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			if (mediaPlayer != null) {
				return mediaPlayer.isPlaying();
			}
			return false;
		}

		@Override
		public void notifyChanged(String action) throws RemoteException {
			service.notifyChange(action);
		}

		@Override
		public int getPlayModel() throws RemoteException {
			return service.getPlayModel();
		}
	};

	/**
	 * ������Ƶ�������߳�������
	 */
	private void getAllAudio() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				audioItems = new ArrayList<AudioItem>();
				// ���ֻ������������Ƶ��Ϣ��ȡ����
				ContentResolver contentResolver = getContentResolver();
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				String projection[] = { MediaStore.Audio.Media.DISPLAY_NAME,// ����
						MediaStore.Audio.Media.DURATION,// ʱ��
						MediaStore.Audio.Media.SIZE,// ��Ƶ�ļ��Ĵ�С
						MediaStore.Audio.Media.DATA,// ��Ƶ��sd���ľ��Ե�ַ-����
						MediaStore.Audio.Media.ARTIST };
				Cursor cursor = contentResolver.query(uri, projection, null,
						null, null);
				while (cursor.moveToNext()) {
					AudioItem item = new AudioItem();

					String title = cursor.getString(0);
					String duration = cursor.getString(1);
					long size = cursor.getLong(2);
					String data = cursor.getString(3);
					String artist = cursor.getString(4);

					item.setTitle(title);
					item.setSize(size);
					item.setData(data);
					item.setDuration(duration);
					item.setArtist(artist);

					audioItems.add(item);

				}
				// mHandler.sendEmptyMessage(0);
			}
		}).start();
	}

	protected int getPlayModel() {
		return playmodel;
	}

	/**
	 * ����λ��ȥ�� һ����Ƶ�ļ�������
	 * 
	 * @param positon
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private void openAudio(int positon) throws Exception {
		currentAudioItem = audioItems.get(positon);
		currentPosition = positon;
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mediaPlayer.setOnCompletionListener(mOnCompletionListene);
		mediaPlayer.setOnErrorListener(mOnErrorListener);
		mediaPlayer.setDataSource(currentAudioItem.getData());
		// �첽׼��
		mediaPlayer.prepareAsync();
	}

	/**
	 * ����
	 */
	@SuppressWarnings("deprecation")
	private void play() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
		int icon = R.drawable.playing;
		// ��״̬��������Ϣ
		long when = System.currentTimeMillis();
		CharSequence tickerText = "���ڲ���:" + getName();
		Notification notification = new Notification(icon, tickerText, when);
		// �������ԣ��������,����ִ��ĳ������
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Intent intent = new Intent(this, AudioPlayerActivity.class);
		intent.putExtra("from_notification", true);
		// ������ͼ
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		// ����֪ͨ������¼�
		notification
				.setLatestEventInfo(this, "�ֻ�Ӱ��", tickerText, contentIntent);
		// һ��Ҫд��
		startForeground(1, notification);
	}

	/**
	 * ��ͣ
	 */
	private void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
		// ��״̬������ʾ���ֲ�����Ч������
		stopForeground(true);
	}

	/**
	 * �õ���������Ϣ
	 * 
	 * @return
	 */
	private String getArtist() {
		if (currentAudioItem != null) {
			return currentAudioItem.getArtist();
		}
		return null;
	}

	/**
	 * �õ��ļ�����
	 * 
	 * @return
	 */
	private String getName() {
		if (currentAudioItem != null) {
			return currentAudioItem.getTitle();
		}
		return null;
	}

	/**
	 * �õ���������ʱ��
	 * 
	 * @return
	 */
	private int getDuration() {
		if (mediaPlayer != null) {
			return mediaPlayer.getDuration();
		}
		return 0;
	}

	/**
	 * �õ���ǰ����λ��
	 * 
	 * @return
	 */
	private int getCurrentPosition() {
		if (mediaPlayer != null) {
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	/**
	 * ��λ����Ƶ�Ĳ���λ��
	 * 
	 * @param position
	 */
	private void seekTo(int position) {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(position);
		}
	}

	/**
	 * ���ø����Ĳ���ģʽ��˳�򣬵�����ȫ��
	 * 
	 * @param model
	 */
	private void setPlayModel(int model) {
		playmodel = model;
		Editor editor = sp.edit();
		editor.putInt("playmode", model);
		editor.commit();
	}

	/**
	 * ��һ��
	 */
	private void playPre() {
		setPrePosition();
		try {
			openPreAudio();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����һ��
	 * 
	 * @throws Exception
	 */
	private void openPreAudio() throws Exception {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// ˳�򲥷�
			if (currentPosition != 0) {
				openAudio(currentPosition);
			} else if (currentPosition == 0 && !isCompletion) {
				openAudio(currentPosition);
			}

		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ����ѭ��
			openAudio(currentPosition);
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// ȫ������
			openAudio(currentPosition);
		}
	}

	/**
	 * ������һ����λ��
	 */
	private void setPrePosition() {

		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// ˳�򲥷�
			currentPosition--;
			if (currentPosition < 0) {
				currentPosition = 0;
			}
		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ����ѭ��
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// ȫ������
			currentPosition--;
			if (currentPosition < 0) {
				currentPosition = audioItems.size() - 1;
			}
		}
	}

	/**
	 * ��һ��
	 */
	private void playNext() {
		setNextPosition();
		try {
			openNextAudio();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����λ�ô���һ��
	 * 
	 * @throws Exception
	 */
	private void openNextAudio() throws Exception {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// ˳�򲥷�
			if (currentPosition != audioItems.size() - 1) {
				openAudio(currentPosition);
			} else if (currentPosition == audioItems.size() - 1
					&& !isCompletion) {
				openAudio(currentPosition);
			}

		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ����ѭ��
			openAudio(currentPosition);
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// ȫ������
			openAudio(currentPosition);
		}
	}

	/**
	 * ������һ����λ��
	 */
	private void setNextPosition() {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// ˳�򲥷�
			currentPosition++;
			if (currentPosition >= audioItems.size() - 1) {
				currentPosition = audioItems.size() - 1;
			}
		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ����ѭ��
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// ȫ������
			currentPosition++;
			if (currentPosition > audioItems.size() - 1) {
				currentPosition = 0;
			}
		}
	}
}
