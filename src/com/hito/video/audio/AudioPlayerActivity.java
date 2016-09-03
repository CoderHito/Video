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
	 * 进度更新
	 */
	protected static final int PROGRESS = 1;
	/**
	 * 歌词更新进度
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
	 * 是否播放中 true:播放中 false:暂停中
	 */
	private boolean isPlaying = true;

	/**
	 * 要播放的音乐文件的位置
	 */
	private int position;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// 得到当前的播放进度
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
	 * 初始化数据
	 */
	private void initData() {
		utils = new Utils();
		// 监听准备好的广播
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
			case R.id.btn_play_pause:// 播放和暂停
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
	 * 设置点击事件
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
	 * 改变播放模式
	 * 
	 * @throws RemoteException
	 */
	protected void changeModel() throws RemoteException {
		// 当前模式
		int model = service.getPlayModel();
		// 改变模式
		if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 单曲循环
			model = MusicPlayService.REPRAT_MODE_CURRENT;
			Toast.makeText(AudioPlayerActivity.this, "单曲循环", 0).show();
		} else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 全部播放
			model = MusicPlayService.REPRAT_MODE_ALL;
			Toast.makeText(AudioPlayerActivity.this, "列表循环", 0).show();
		} else if (model == MusicPlayService.REPRAT_MODE_ALL) {
			// 默认
			model = MusicPlayService.REPRAT_MODE_NORMAL;
			Toast.makeText(AudioPlayerActivity.this, "播放全部", 0).show();
		}
		// 设置模式
		service.setPlayModel(model);
		setPlayModelButton();
	}

	// /**
	// * 设置按钮的状态
	// *
	// * @throws RemoteException
	// */
	// private void setPlayModelButtonStatus() throws RemoteException {
	// // 当前模式
	// int model = service.getPlayModel();
	// // 改变模式
	// if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
	// // 默认
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_normal_selector);
	// } else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
	// // 单曲循环
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_single_selector);
	//
	// } else if (model == MusicPlayService.REPRAT_MODE_ALL) {
	// // 全部播放
	// btn_model
	// .setBackgroundResource(R.drawable.btn_audio_model_all_selector);
	//
	// }
	// }

	/**
	 * 设置按钮的状态
	 * 
	 * @throws RemoteException
	 */
	private void setPlayModelButton() throws RemoteException {
		// 当前模式
		int model = service.getPlayModel();
		// 改变模式
		if (model == MusicPlayService.REPRAT_MODE_NORMAL) {
			// 默认
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_normal_selector);
		} else if (model == MusicPlayService.REPRAT_MODE_CURRENT) {
			// 单曲循环
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_single_selector);

		} else if (model == MusicPlayService.REPRAT_MODE_ALL) {
			// 全部播放
			btn_model
					.setBackgroundResource(R.drawable.btn_audio_model_all_selector);

		}
	}

	/**
	 * 设置按钮的状态信息
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
	 * 设置View的状态
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
		// 发送消息,开始更新音频的进度
		handler.sendEmptyMessage(PROGRESS);
		// 发消息更新移动歌词
		handler.sendEmptyMessage(LYRIC_PROGRESS);

	}

	/**
	 * 初始化View
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
		setTitle("音乐播放");
		setRightButton(View.GONE);
		image = (ImageView) findViewById(R.id.iv_audio_play_image);
		ad = (AnimationDrawable) image.getBackground();
		ad.start();
	}

	/**
	 * 得到音乐文件的位置
	 */
	private void getData() {

		Intent intent = getIntent();
		from_notification = intent.getBooleanExtra("from_notification", false);
		// 是来自状态栏的intent就不去设置位置，解决 点击一次状态栏就会重新播放(第一首)的bug
		if (!from_notification) {
			position = intent.getIntExtra("position", 0);
		}
	}

	/**
	 * 代表了服务，通过它得到服务里面的信息
	 */
	private IMusicPlayService service;
	private ServiceConnection conn = new ServiceConnection() {
		/**
		 * 取消绑定的时候回调这个方法
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}

		/**
		 * 绑定成功的时候回调这个方法
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			service = IMusicPlayService.Stub.asInterface(iBinder);
			if (service != null) {
				try {
					// intent来自于状态栏则不去重新打开音乐文件
					if (!from_notification) {
						service.openAudio(position);
					} else {
						// 发送一个消息，告诉Activity已经准备好播放
						service.notifyChanged(MusicPlayService.PREPARED_MSG);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
	/**
	 * 是否来自状态栏 true：来自状态栏 false：不是来自状态栏的
	 */
	private boolean from_notification;

	/**
	 * 以绑定的方式启动服务
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
