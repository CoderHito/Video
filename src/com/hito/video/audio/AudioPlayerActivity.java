package com.hito.video.audio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hito.video.BaseActivity;
import com.hito.video.R;
import com.hito.video.utils.Utils;
import com.hito.video.video.videoview.ShowLyricTextView;

public class AudioPlayerActivity extends BaseActivity {
	/**
	 * ���ȸ���
	 */
	protected static final int PROGRESS = 1;
	/**
	 * ��ʸ��½���
	 */
	protected static final int LYRIC_PROGRESS = 2;
	private AnimationDrawable ad;
	private ImageView image;
	private TextView tv_artist;
	private TextView tv_music_name;
	private TextView tv_music_time;
	private SeekBar seekbar_audio;
	private Button btn_model;
	private Button btn_pre;
	private Button btn_play_pause;
	private Button btn_next;
	private Button btn_lyric;
	private MyBroadcastReceiver receiver;
	private Utils utils;
	private ShowLyricTextView show_lyric_view;

	/**
	 * �Ƿ񲥷��� true:������ false:��ͣ��
	 */
	private boolean isPlaying = true;

	/**
	 * Ҫ���ŵ������ļ���λ��
	 */
	private int position;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// �õ���ǰ�Ĳ��Ž���
				try {
					int currentPosition = service.getCurrentPosition();
					seekbar_audio.setProgress(currentPosition);
					tv_music_time.setText(utils.stringFroTime(service
							.getCurrentPosition())
							+ "/"
							+ utils.stringFroTime(service.getDuration()));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				if (!isDestory) {
					handler.sendEmptyMessageDelayed(PROGRESS, 1000);
				}
				break;
			case LYRIC_PROGRESS:

				try {
					int currentPosition = service.getCurrentPosition();
					show_lyric_view.setShowNextLyric(currentPosition);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				handler.removeMessages(LYRIC_PROGRESS);
				handler.sendEmptyMessage(LYRIC_PROGRESS);
				break;

			default:
				break;
			}
		};
	};

	private boolean isDestory = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
		setListener();
		getData();
		bindService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isDestory = true;
		unregisterReceiver(receiver);
		receiver = null;
	}

	/**
	 * ��ʼ������
	 */
	private void initData() {
		utils = new Utils();
		// ����׼���õĹ㲥
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicPlayService.PREPARED_MSG);
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				setViewStatus();
				setPlayModelButton();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_next:
				try {
					service.playNext();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				break;
			case R.id.btn_pre:
				try {
					service.playPre();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				break;
			case R.id.btn_play_pause:// ���ź���ͣ
				try {
					if (isPlaying) {
						service.pause();
						// btn_play_pause
						// .setBackgroundResource(R.drawable.btn_audio_play_selector);
					} else {
						service.play();
						// btn_play_pause
						// .setBackgroundResource(R.drawable.btn_audio_pause_selector);
					}
					isPlaying = !isPlaying;
					setButtonStatus();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case R.id.btn_model:
				try {
					changeModel();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
		}
	};

	/**
	 * ���õ���¼�
	 */
	private void setListener() {
		btn_next.setOnClickListener(mClickListener);
		btn_pre.setOnClickListener(mClickListener);
		btn_model.setOnClickListener(mClickListener);
		btn_play_pause.setOnClickListener(mClickListener);
		seekbar_audio.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					try {
						service.seekTo(progress);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * �ı䲥��ģʽ
	 * 
	 * @throws RemoteException
	 */
	protected void changeModel() throws RemoteException {
		// ��ǰģʽ
		int model = service.getPlayModel();
		// �ı�ģʽ
		if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
			// ����ѭ��
			model = MusicPlayService.REPRAT_MODE_CURRENT;
			Toast.makeText(AudioPlayerActivity.this, "����ѭ��", 0).show();
		} else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ȫ������
			model = MusicPlayService.REPRAT_MODE_ALL;
			Toast.makeText(AudioPlayerActivity.this, "�б�ѭ��", 0).show();
		} else if (model == MusicPlayService.REPRAT_MODE_ALL) {
			// Ĭ��
			model = MusicPlayService.REPRAT_MODE_NORMAL;
			Toast.makeText(AudioPlayerActivity.this, "����ȫ��", 0).show();
		}
		// ����ģʽ
		service.setPlayModel(model);
		setPlayModelButton();
	}

	// /**
	// * ���ð�ť��״̬
	// *
	// * @throws RemoteException
	// */
	// private void setPlayModelButtonStatus() throws RemoteException {
	// // ��ǰģʽ
	// int model = service.getPlayModel();
	// // �ı�ģʽ
	// if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
	// // Ĭ��
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_normal_selector);
	// } else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
	// // ����ѭ��
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_single_selector);
	//
	// } else if (model == MusicPlayService.REPRAT_MODE_ALL) {
	// // ȫ������
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_all_selector);
	//
	// }
	// }

	/**
	 * ���ð�ť��״̬
	 * 
	 * @throws RemoteException
	 */
	private void setPlayModelButton() throws RemoteException {
		// ��ǰģʽ
		int model = service.getPlayModel();
		// �ı�ģʽ
		if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
			// Ĭ��
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_normal_selector);
		} else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
			// ����ѭ��
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_single_selector);

		} else if (model == MusicPlayService.REPRAT_MODE_ALL) {
			// ȫ������
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_all_selector);

		}
	}

	/**
	 * ���ð�ť��״̬��Ϣ
	 */
	protected void setButtonStatus() {
		if (isPlaying) {
			btn_play_pause
					.setBackgroundResource(R.drawable.btn_audio_pause_selector);
		} else {
			btn_play_pause
					.setBackgroundResource(R.drawable.btn_audio_play_selector);
		}
	}

	/**
	 * ����View��״̬
	 * 
	 * @throws RemoteException
	 */
	public void setViewStatus() throws RemoteException {
		System.out.println("-------------" + service.getArtist());
		tv_artist.setText(service.getArtist());
		tv_music_name.setText(service.getName());
		tv_music_time.setText(utils.stringFroTime(service.getCurrentPosition())
				+ "/" + utils.stringFroTime(service.getDuration()));
		seekbar_audio.setMax(service.getDuration());
		isPlaying = service.isPlaying();
		setButtonStatus();
		// ������Ϣ,��ʼ������Ƶ�Ľ���
		handler.sendEmptyMessage(PROGRESS);
		// ����Ϣ�����ƶ����
		handler.sendEmptyMessage(LYRIC_PROGRESS);

	}

	/**
	 * ��ʼ��View
	 */
	public void initView() {
		show_lyric_view = (ShowLyricTextView) findViewById(R.id.show_lyric_view);
		tv_artist = (TextView) findViewById(R.id.tv_artist);
		tv_music_name = (TextView) findViewById(R.id.tv_music_name);
		tv_music_time = (TextView) findViewById(R.id.tv_music_time);
		seekbar_audio = (SeekBar) findViewById(R.id.seekbar_audio);
		btn_model = (Button) findViewById(R.id.btn_model);
		btn_pre = (Button) findViewById(R.id.btn_pre);
		btn_play_pause = (Button) findViewById(R.id.btn_play_pause);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_lyric = (Button) findViewById(R.id.btn_lyric);
		setTitle("���ֲ���");
		setRightButton(View.GONE);
		image = (ImageView) findViewById(R.id.iv_audio_play_image);
		ad = (AnimationDrawable) image.getBackground();
		ad.start();
	}

	/**
	 * �õ������ļ���λ��
	 */
	private void getData() {

		Intent intent = getIntent();
		from_notification = intent.getBooleanExtra("from_notification", false);
		// ������״̬����intent�Ͳ�ȥ����λ�ã���� ���һ��״̬���ͻ����²���(��һ��)��bug
		if (!from_notification) {
			position = intent.getIntExtra("position", 0);
		}
	}

	/**
	 * �����˷���ͨ�����õ������������Ϣ
	 */
	private IMusicPlayService service;
	private ServiceConnection conn = new ServiceConnection() {
		/**
		 * ȡ���󶨵�ʱ��ص��������
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}

		/**
		 * �󶨳ɹ���ʱ��ص��������
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			service = IMusicPlayService.Stub.asInterface(iBinder);
			if (service != null) {
				try {
					// intent������״̬����ȥ���´������ļ�
					if (!from_notification) {
						service.openAudio(position);
					} else {
						// ����һ����Ϣ������Activity�Ѿ�׼���ò���
						service.notifyChanged(MusicPlayService.PREPARED_MSG);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
	/**
	 * �Ƿ�����״̬�� true������״̬�� false����������״̬����
	 */
	private boolean from_notification;

	/**
	 * �԰󶨵ķ�ʽ��������
	 */
	private void bindService() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		intent.setAction("com.hito.musicplayer.bindservice");
		intent.putExtras(bundle);

		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		startService(intent);
	}

	@Override
	public View setChildContentView() {
		View view = View.inflate(AudioPlayerActivity.this,
				R.layout.activity_audio_play, null);
		return view;
	}

	@Override
	public void rightButtonClick() {

	}

	@Override
	public void leftButtonClick() {
		finish();
	}

}
