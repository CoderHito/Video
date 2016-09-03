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
	 * 视频播放准备完成的时候发送的消息
	 */
	protected static final String PREPARED_MSG = "com.hito.musicplayer.prepared";
	private ArrayList<AudioItem> audioItems;
	private SharedPreferences sp;
	/**
	 * 当前音频信息
	 */
	private AudioItem currentAudioItem;
	/**
	 * 当前音频位置
	 */
	private int currentPosition;
	/**
	 * 媒体播放器,可以播放本地视频，本地音乐，网络视频，网络音乐
	 */
	private MediaPlayer mediaPlayer;

	/**
	 * 默认模式 --顺序循环
	 */
	public static int REPRAT_MODE_NORMAL = 0;
	/**
	 * 单曲循环
	 */
	public static int REPRAT_MODE_CURRENT = 1;
	/**
	 * 播放全部
	 */
	public static int REPRAT_MODE_ALL = 2;

	/**
	 * 播放模式
	 */
	private int playmodel = REPRAT_MODE_NORMAL;
	/**
	 * 是否播放完成
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
	 * 加载数据，保存上一次的播放模式
	 */
	private void initData() {
		sp = getSharedPreferences("config", MODE_PRIVATE);
		playmodel = sp.getInt("platmode", REPRAT_MODE_NORMAL);
		getAllAudio();

	}

	/**
	 * 发送广播
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
			Toast.makeText(getApplicationContext(), "播放出错了..", 0).show();
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
	 * 加载视频，在子线程中运行
	 */
	private void getAllAudio() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				audioItems = new ArrayList<AudioItem>();
				// 把手机里面的所有视频信息读取出来
				ContentResolver contentResolver = getContentResolver();
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				String projection[] = { MediaStore.Audio.Media.DISPLAY_NAME,// 标题
						MediaStore.Audio.Media.DURATION,// 时长
						MediaStore.Audio.Media.SIZE,// 视频文件的大小
						MediaStore.Audio.Media.DATA,// 视频在sd卡的绝对地址-播放
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
	 * 根据位置去打开 一个音频文件并播放
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
		// 异步准备
		mediaPlayer.prepareAsync();
	}

	/**
	 * 播放
	 */
	@SuppressWarnings("deprecation")
	private void play() {
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
		int icon = R.drawable.playing;
		// 在状态栏弹出消息
		long when = System.currentTimeMillis();
		CharSequence tickerText = "正在播放:" + getName();
		Notification notification = new Notification(icon, tickerText, when);
		// 设置属性，点击后还在,而且执行某个任务
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		Intent intent = new Intent(this, AudioPlayerActivity.class);
		intent.putExtra("from_notification", true);
		// 延期意图
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		// 设置通知栏点击事件
		notification
				.setLatestEventInfo(this, "手机影音", tickerText, contentIntent);
		// 一定要写的
		startForeground(1, notification);
	}

	/**
	 * 暂停
	 */
	private void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
		// 把状态栏的显示音乐播放器效果消掉
		stopForeground(true);
	}

	/**
	 * 得到艺术家信息
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
	 * 得到文件名称
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
	 * 得到歌曲的总时长
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
	 * 得到当前播放位置
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
	 * 定位到音频的播放位置
	 * 
	 * @param position
	 */
	private void seekTo(int position) {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(position);
		}
	}

	/**
	 * 设置歌曲的播放模式：顺序，单曲，全部
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
	 * 上一曲
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
	 * 打开上一曲
	 * 
	 * @throws Exception
	 */
	private void openPreAudio() throws Exception {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 顺序播放
			if (currentPosition != 0) {
				openAudio(currentPosition);
			} else if (currentPosition == 0 && !isCompletion) {
				openAudio(currentPosition);
			}

		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 单曲循环
			openAudio(currentPosition);
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// 全部播放
			openAudio(currentPosition);
		}
	}

	/**
	 * 设置上一曲的位置
	 */
	private void setPrePosition() {

		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 顺序播放
			currentPosition--;
			if (currentPosition < 0) {
				currentPosition = 0;
			}
		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 单曲循环
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// 全部播放
			currentPosition--;
			if (currentPosition < 0) {
				currentPosition = audioItems.size() - 1;
			}
		}
	}

	/**
	 * 下一曲
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
	 * 根据位置打开下一曲
	 * 
	 * @throws Exception
	 */
	private void openNextAudio() throws Exception {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 顺序播放
			if (currentPosition != audioItems.size() - 1) {
				openAudio(currentPosition);
			} else if (currentPosition == audioItems.size() - 1
					&& !isCompletion) {
				openAudio(currentPosition);
			}

		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 单曲循环
			openAudio(currentPosition);
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// 全部播放
			openAudio(currentPosition);
		}
	}

	/**
	 * 设置下一曲的位置
	 */
	private void setNextPosition() {
		if (playmodel == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 顺序播放
			currentPosition++;
			if (currentPosition >= audioItems.size() - 1) {
				currentPosition = audioItems.size() - 1;
			}
		} else if (playmodel == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 单曲循环
		} else if (playmodel == MusicPlayService.REPRAT_MODE_ALL) {
			// 全部播放
			currentPosition++;
			if (currentPosition > audioItems.size() - 1) {
				currentPosition = 0;
			}
		}
	}
}
