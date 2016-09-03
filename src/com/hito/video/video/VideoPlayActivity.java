package com.hito.video.video;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.hito.video.BaseActivity;
import com.hito.video.R;
import com.hito.video.utils.Utils;
import com.hito.video.video.domain.VideoItem;
import com.hito.video.video.videoview.VideoView;

/**
 * 视频播放器--用系统api写的 ---播放能力和系统自带的播放器一样的功能
 * 
 * @author hito
 * 
 */
public class VideoPlayActivity extends BaseActivity {

	/**
	 * 更新进度
	 */
	protected static final int PROGRESS = 1;
	/**
	 * 隐藏控制面板
	 */
	protected static final int DELAYED_HIDE_CONTROL_PLAYER = 2;
	/**
	 * 全屏显示
	 */
	private static final int FULL_SCREEN = 3;

	/**
	 * 默认屏幕
	 */
	private static final int DEFAULT_SCREEN = 4;
	/**
	 * 延迟两秒关闭当前播放器
	 */
	private static final int FINISH = 5;

	/**
	 * 是否是全屏
	 * 
	 * true：全屏
	 * 
	 * false：默认
	 */
	private boolean isFullScreen = false;
	private VideoView videoView;
	private Uri uri;// 视频地址

	private TextView tv_video_title, tv_system_time, tv_current_time,
			tv_duration;
	private ImageView iv_battery;
	private Button btn_voice, btn_switch, btn_exit, btn_pre, btn_play_pause,
			btn_next, btn_screen;
	private SeekBar seekbar_voice, seekbar_video;
	private Utils utils;
	private MyBroadcastReceiver receiver;
	/**
	 * 视频列表
	 */
	private ArrayList<VideoItem> videoItems;
	/**
	 * 视频当前位置，要播放的视频的位置
	 */
	private int position;
	/**
	 * 系统电量范围值0-100
	 */
	private int level;
	/**
	 * 判断是否是播放状态 true：为播放状态 false：为暂停状态
	 */
	private boolean isPlay = false;
	/**
	 * 判断当前Activity是否被销毁 true：已经被销毁 false：没有被销毁
	 */
	private boolean isDestory = false;

	private GestureDetector detector;
	private LinearLayout ll_control_player;

	/**
	 * 是否显示控制面板
	 * 
	 * true： 显示 false：隐藏
	 */
	private boolean isShowControl = false;

	private WindowManager wm;
	/**
	 * 屏幕的宽
	 */
	private int screenWidth;
	/**
	 * 屏幕的高
	 */
	private int screenHeight;

	/**
	 * 管理音量大小
	 */
	private AudioManager am;

	/**
	 * 当前音量
	 */
	private int currentVolume;
	/**
	 * 最大音量
	 */
	private int maxVolume;

	/**
	 * 当前是否是静音
	 * 
	 * true:是静音
	 * 
	 * false:非静音
	 */
	private boolean isMute = false;

	/**
	 * 视频加载
	 */
	private LinearLayout ll_loading;
	/**
	 * 视频卡顿
	 */
	private LinearLayout ll_buffering;
	/**
	 * true：卡了但是还没有把卡效果消除
	 * 
	 * false：卡了但是把卡的效果消除了
	 * 
	 */
	private boolean isBuffing = false;
	/**
	 * 手指在屏幕上滑动的起始位置
	 */
	private float startY;
	/**
	 * 屏幕滑动的范围
	 */
	private float audioTouchRang;
	/**
	 * 滑动前的音量值
	 */
	private int mVol;
	/**
	 * 判断当前播放路径是否来源于网络
	 */
	private boolean isNetUri;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// 得到视频当前播放进度
				int currentPosition = videoView.getCurrentPosition();
				tv_current_time.setText(utils.stringFroTime(currentPosition));
				// 2,seekbar更新进度
				seekbar_video.setProgress(currentPosition);

				// 设置电量的显示
				setBattery();
				// 设置显示当前手机的时间
				tv_system_time.setText(utils.getSystemTime());
				// 判断是否是来源于网络,是则加载缓冲进度，不是则不加载缓冲进度
				if (isNetUri) {
					// 设置缓冲进度
					// 缓冲比例值： 0 - 100 之间
					int percentage = videoView.getBufferPercentage();
					int total = percentage * seekbar_video.getMax();
					int secondaryProgress = total / 100;
					seekbar_video.setSecondaryProgress(secondaryProgress);
				} else {
					seekbar_video.setSecondaryProgress(0);
				}

				// 消息的死循环
				if (!isDestory) {
					mHandler.removeMessages(PROGRESS);
					mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
				}
				break;
			case DELAYED_HIDE_CONTROL_PLAYER:
				hideControlPlayer();
				break;
			case FINISH:// 关闭当前页面
				if (videoView != null) {
					videoView.stopPlayback();
				}
				finish();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initView();
		getData();
		setData();
		setListener();
		// 添加控制栏
		// videoView.setMediaController(new MediaController(this));
	}

	/**
	 * 设置数据
	 */
	private void setData() {
		if (videoItems != null && videoItems.size() > 0) {
			VideoItem videoItem = videoItems.get(position);
			videoView.setVideoPath(videoItem.getData());
			isNetUri = utils.isNetUri(videoItems.toString());
			tv_video_title.setText(videoItem.getTitle());
		} else if (uri != null) {
			// 设置播放地址
			videoView.setVideoURI(uri);
			// 设置标题
			tv_video_title.setText(uri.toString());
			isNetUri = utils.isNetUri(uri.toString());
			btn_pre.setBackgroundResource(R.drawable.btn_pre_gray);
			btn_pre.setEnabled(false);

			btn_next.setBackgroundResource(R.drawable.btn_next_gray);
			btn_next.setEnabled(false);
		}
		// seekbar和音量总大小进行关联
		seekbar_voice.setMax(maxVolume);
		seekbar_voice.setProgress(currentVolume);
		Log.e("volume", currentVolume + "");
	}

	/**
	 * 得到数据
	 */
	private void getData() {
		// 得到播放列表
		Intent intent = getIntent();
		videoItems = (ArrayList<VideoItem>) intent
				.getSerializableExtra("videolist");
		intent.getIntExtra("position", 0);

		// 得到播放地址 一般是第三方软件
		uri = getIntent().getData();
		// videoView.setVideoURI(uri);
	}

	@SuppressWarnings("deprecation")
	private void initData() {
		isDestory = false;
		utils = new Utils();

		// 设置当播放视频的时候不锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// 得到屏幕 高和宽
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		// 监听电量变化
		receiver = new MyBroadcastReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);// 电量变化的时候，系统会发送这个广播
		registerReceiver(receiver, filter);
		// 实例化手势识别器
		detector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					/**
					 * 长按事件
					 */
					@Override
					public void onLongPress(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "长按屏幕", 0)
						// .show();
						super.onLongPress(e);
						startOrPause();
					}

					/**
					 * 双击事件
					 */
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						// Toast.makeText(getApplicationContext(), "双击屏幕", 0)
						// .show();
						// super.onDoubleTap(e);
						if (isFullScreen) {
							setVideoType(DEFAULT_SCREEN);
						} else {
							setVideoType(FULL_SCREEN);
						}
						return true;
					}

					/**
					 * 单击事件
					 */
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "单机屏幕", 0)
						// .show();
						super.onSingleTapConfirmed(e);
						if (isShowControl) {
							// 如果之前已经隐藏了，则移除之前的消息
							removeDelayedHideControlPlayer();
							hideControlPlayer();

						} else {
							showControlPlayer();
							// 发送消息隐藏控制面板
							SendDelayedHideControlPlayer();
						}
						return true;
					}

				});

		// 得到当前音量和最大音量值
		am = (AudioManager) getSystemService(AUDIO_SERVICE);
		// 得到当前音量大小
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		// 最大音量值 范围0-15之间
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

	}

	/**
	 * 发送消息
	 * 
	 * 延迟5s隐藏控制面板
	 */
	private void SendDelayedHideControlPlayer() {
		mHandler.sendEmptyMessageDelayed(DELAYED_HIDE_CONTROL_PLAYER, 5000);
	}

	/**
	 * 移除延迟隐藏控制面板的消息
	 */
	protected void removeDelayedHideControlPlayer() {
		mHandler.removeMessages(DELAYED_HIDE_CONTROL_PLAYER);
	}

	/**
	 * 使用手势识别器
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 手指按下屏幕
			removeDelayedHideControlPlayer();
			// 记录初始值
			startY = event.getY();
			audioTouchRang = Math.min(screenHeight, screenWidth);
			mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			break;
		case MotionEvent.ACTION_MOVE:
			/**
			 * 记录endY
			 */
			float endY = event.getY();
			/**
			 * 计算偏移量
			 */
			float distanceY = startY - endY;
			/**
			 * 计算屏幕滑动比例
			 */
			float detal = distanceY / audioTouchRang;
			/**
			 * 计算改变的音量值
			 * 
			 * 改变的音量 = 欢动的距离/总距离*总音量
			 */
			float volume = distanceY / audioTouchRang * maxVolume;
			/**
			 * 屏蔽非法值 找出要设置的音量值
			 */
			float volmeS = Math.min(Math.max(volume + mVol, 0), maxVolume);

			if (detal != 0) {
				updateVoice((int) volmeS);
			}

			break;
		case MotionEvent.ACTION_UP:// 手指离开
			SendDelayedHideControlPlayer();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 设置电量的变化
	 */
	private void setBattery() {
		if (level <= 0) {
			iv_battery.setImageResource(R.drawable.ic_battery_0);
		} else if (level <= 10) {
			iv_battery.setImageResource(R.drawable.ic_battery_10);
		} else if (level <= 20) {
			iv_battery.setImageResource(R.drawable.ic_battery_20);
		} else if (level <= 40) {
			iv_battery.setImageResource(R.drawable.ic_battery_40);
		} else if (level <= 60) {
			iv_battery.setImageResource(R.drawable.ic_battery_60);
		} else if (level <= 80) {
			iv_battery.setImageResource(R.drawable.ic_battery_80);
		} else if (level <= 100) {
			iv_battery.setImageResource(R.drawable.ic_battery_100);
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 得到电量的值 0-100
			level = intent.getIntExtra("level", 0);
		}

	}

	protected void onDestroy() {
		super.onDestroy();
		isDestory = true;
		// 取消电量监听
		unregisterReceiver(receiver);
		receiver = null;
	};

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			removeDelayedHideControlPlayer();
			SendDelayedHideControlPlayer();
			switch (v.getId()) {
			case R.id.btn_exit:
				mHandler.sendEmptyMessage(FINISH);
				break;
			case R.id.btn_play_pause:// 播放和暂停的切换
				// 播放或者暂停
				startOrPause();
				break;
			case R.id.btn_next:// 下一步
				playNextVideo();
				break;
			case R.id.btn_pre:// 上一步
				playPreVideo();
				break;
			case R.id.btn_screen:
				if (isFullScreen) {
					setVideoType(DEFAULT_SCREEN);
				} else {
					setVideoType(FULL_SCREEN);
				}
				break;
			case R.id.btn_voice:
				// 设置静音和非静音
				isMute = !isMute;
				updateVoice(currentVolume);
				break;
			case R.id.btn_switch:
				new AlertDialog.Builder(VideoPlayActivity.this)
						.setMessage("当前是系统播放器，是否切换到万能播放器")
						.setNegativeButton("下次再说", null)
						.setPositiveButton("立即切换",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										startVitamioPlayer();
									}
								}).setCancelable(false).show();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 设置各种监听
	 */
	private void setListener() {

		// 设置按钮的监听
		btn_play_pause.setOnClickListener(mOnClickListener);

		btn_next.setOnClickListener(mOnClickListener);
		btn_pre.setOnClickListener(mOnClickListener);

		btn_screen.setOnClickListener(mOnClickListener);
		btn_voice.setOnClickListener(mOnClickListener);
		btn_switch.setOnClickListener(mOnClickListener);
		btn_exit.setOnClickListener(mOnClickListener);
		// 设置seekbar状态改变的监听，调节音量大小
		seekbar_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// 开始滑动的时候取消隐藏界面的消息
				removeDelayedHideControlPlayer();
				Log.e("volume", "开始");
			}

			/**
			 * 调节音量大小
			 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					updateVoice(progress);
					Log.e("volume", progress + "");
				}
			}
		});
		// 视频进度
		seekbar_video.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			/**
			 * 手指离开seekbar的时候回调的方法
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				SendDelayedHideControlPlayer();
			}

			/**
			 * seekbar上拖动的时候回调的方法
			 * */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				removeDelayedHideControlPlayer();
			}

			/**
			 * 当seekbar状态发生改变的时候回调这个方法
			 * 
			 * seekbar:自身
			 * 
			 * progress:seekbar的位置，视频的长度和seekbar长度一一对应
			 * 
			 * fromUser：seekbar滑动的时候为true
			 * */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					// 拖动到具体的位置
					videoView.seekTo(progress);
				}
			}
		});

		// 监听视频是否准备播放--开始播放
		videoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// 开始播放
				videoView.start();
				isPlay = true;
				// 得到视频长度
				int duration = videoView.getDuration();
				tv_duration.setText(utils.stringFroTime(duration));

				// 1,视频总时长 关联seekbar
				seekbar_video.setMax(duration);

				// 设置屏幕大小为默认
				setVideoType(DEFAULT_SCREEN);

				// 设置隐藏控制面板
				hideControlPlayer();
				// 隐藏加载效果
				ll_loading.setVisibility(View.GONE);
				// 更新进度
				mHandler.sendEmptyMessage(PROGRESS);
			}
		});

		// 设置播放完成监听
		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playNextVideo();
			}
		});

		// 设置监听是否播放出错了
		videoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				/**
				 * 视频播放出错的原因有哪些？
				 * 
				 * 播放的视频格式不支持---采用万能播放器播放
				 * 
				 * 播放网络视频，网络断了---采用重新播放的方式
				 * 
				 * 下载的视频文件不完整，有空白---尽量避免，修复下载的bug
				 * 
				 * 播放一个不是视频的文件---提示用户
				 * 
				 * 只要出错了，都会回调这个方法
				 */
				// Toast.makeText(getApplicationContext(), "播放出错了...",
				// 0).show();
				// 播放出错，启动万能播放器
				startVitamioPlayer();
				return true;
			}
		});

		// 监听播放卡顿,并且提示用户Android2.3以后才有的
		videoView.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:// 卡了 或者 拖动卡了
					// Toast.makeText(getApplicationContext(), "卡了", 1).show();
					ll_buffering.setVisibility(View.VISIBLE);
					isBuffing = true;
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:// 卡结束 或者 拖动卡 结束
					// Toast.makeText(getApplicationContext(), "卡结束", 1).show();
					ll_buffering.setVisibility(View.GONE);
					isBuffing = false;
					break;
				default:
					break;
				}
				return false;
			}
		});
		isBuffing = false;
		/**
		 * 设置监听拖动完成
		 * 
		 * 出现bug：已经卡顿结束但是进度条没有消除
		 */
		videoView.setOnSeekCompleteListener(new OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (isBuffing) {
					ll_buffering.setVisibility(View.GONE);
				}
			}
		});

	}

	/**
	 * 启动万能播放器
	 */
	protected void startVitamioPlayer() {
		Intent intent = new Intent(VideoPlayActivity.this,
				VitamioPlayActivity.class);
		Bundle extras = new Bundle();
		extras.putSerializable("videolist", videoItems);
		intent.putExtras(extras);
		intent.putExtra("position", position);
		intent.setData(uri);
		startActivity(intent);
		// 关闭当前的Activity-播放器---
		// finish();
		// 立刻关闭会导致系统播放器重新被创建---延迟两秒进入系统播放器
		mHandler.sendEmptyMessageDelayed(FINISH, 2000);
	}

	/**
	 * 调节音量的方法
	 * 
	 * @param volume
	 *            ：要调节成的音量值
	 */
	protected void updateVoice(int volume) {
		if (isMute) {
			// 静音
			// 最后一个参数改成1的话，随着拖动条的改变，系统的声音也会发生改变
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			seekbar_voice.setProgress(0);
		} else {
			// 非静音
			am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
			seekbar_voice.setProgress(volume);
		}
		currentVolume = volume;
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		setTitleBar(View.GONE);// 隐藏标题栏
		videoView = (VideoView) findViewById(R.id.videoview);
		tv_video_title = (TextView) findViewById(R.id.tv_video_title);
		tv_system_time = (TextView) findViewById(R.id.tv_system_time);
		tv_current_time = (TextView) findViewById(R.id.tv_current_time);
		tv_duration = (TextView) findViewById(R.id.tv_duration);
		iv_battery = (ImageView) findViewById(R.id.iv_battery);
		btn_voice = (Button) findViewById(R.id.btn_voice);
		btn_switch = (Button) findViewById(R.id.btn_switch);
		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_pre = (Button) findViewById(R.id.btn_pre);
		btn_play_pause = (Button) findViewById(R.id.btn_play_pause);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_screen = (Button) findViewById(R.id.btn_screen);
		seekbar_voice = (SeekBar) findViewById(R.id.seekbar_voice);
		seekbar_video = (SeekBar) findViewById(R.id.seekbar_video);
		ll_control_player = (LinearLayout) findViewById(R.id.ll_control_player);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		ll_buffering = (LinearLayout) findViewById(R.id.ll_buffering);
	}

	/**
	 * 隐藏控制面板
	 */
	private void hideControlPlayer() {
		ll_control_player.setVisibility(View.GONE);
		isShowControl = false;
	}

	/**
	 * 显示控制面板
	 */
	private void showControlPlayer() {
		ll_control_player.setVisibility(View.VISIBLE);
		isShowControl = true;
	}

	@Override
	public View setChildContentView() {
		return View.inflate(this, R.layout.activity_video_play, null);
	}

	@Override
	public void rightButtonClick() {

	}

	@Override
	public void leftButtonClick() {

	}

	/**
	 * 设置按钮的状态
	 */
	private void setPlayOrPauseStatus() {
		// 如果是最后一个视频，下一个按钮就不可以点击，并且变灰
		if (position == 0) {
			// 第一个视频,上一步不可点
			btn_pre.setBackgroundResource(R.drawable.video_pre_gray);
			btn_pre.setEnabled(false);
		} else if (position == videoItems.size() - 1) {
			// 最后一个视频
			// btn_next.setBackgroundResource(R.drawable.video_next_gray);
			btn_next.setEnabled(false);
		} else {
			btn_pre.setBackgroundResource(R.drawable.btn_pre_selector);
			btn_pre.setEnabled(true);

			btn_next.setBackgroundResource(R.drawable.btn_next_selector);
			btn_next.setEnabled(true);
		}
	}

	/**
	 * 播放下一个视频
	 */
	private void playNextVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position++;// 下一个视频
			if (position < videoItems.size()) {
				VideoItem videoItem = videoItems.get(position);
				videoView.setVideoPath(videoItem.getData());
				isNetUri = utils.isNetUri(videoItem.getData().toString());
				// 设置标题
				tv_video_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {

				position = videoItems.size() - 1;
				Toast.makeText(getApplicationContext(), "最后一个视频", 0).show();
				finish();// 退出播放器
			}
		} else if (uri != null) {
			Toast.makeText(getApplicationContext(), "播放完成", 0).show();
			finish();// 退出播放器
		}
	}

	/**
	 * 播放上一个视频
	 */
	private void playPreVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position--;// 上一个视频
			if (position >= 0) {
				VideoItem videoItem = videoItems.get(position);
				isNetUri = utils.isNetUri(videoItem.getData().toString());
				videoView.setVideoPath(videoItem.getData());
				// 设置标题
				tv_video_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {

				position = 0;
				Toast.makeText(getApplicationContext(), "已经是第一个视频", 0).show();
			}

		}
	}

	/**
	 * 播放或者暂停
	 */
	private void startOrPause() {
		if (isPlay) {
			// 暂停
			videoView.pause();
			// 按钮状态要设置为播放
			btn_play_pause.setBackgroundResource(R.drawable.btn_play_selector);
		} else {
			// 播放
			// 按钮状态要设置为暂停
			videoView.start();
			btn_play_pause.setBackgroundResource(R.drawable.btn_pause_selector);
		}
		isPlay = !isPlay;
	}

	/**
	 * 设置视频的类型：全屏和默认
	 * 
	 * @param type
	 */
	public void setVideoType(int type) {
		switch (type) {
		case FULL_SCREEN:// 全屏

			videoView.setVideoSize(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = true;
			btn_screen
					.setBackgroundResource(R.drawable.btn_default_screen_selector);
			break;
		case DEFAULT_SCREEN:// 默认
			int mVideoHeight = videoView.getVideoHeight();
			int mVideoWidth = videoView.getVideoWidth();
			// 计算后视频该设置的宽高
			int height = screenHeight;
			int width = screenWidth;
			if (mVideoWidth > 0 && mVideoHeight > 0) {
				if (mVideoWidth * height > width * mVideoHeight) {
					// Log.i("@@@", "image too tall, correcting");
					height = width * mVideoHeight / mVideoWidth;
				} else if (mVideoWidth * height < width * mVideoHeight) {
					// Log.i("@@@", "image too wide, correcting");
					width = height * mVideoWidth / mVideoHeight;
				} else {
					// Log.i("@@@", "aspect ratio is correct: " +
					// width+"/"+height+"="+
					// mVideoWidth+"/"+mVideoHeight);
				}
			}
			// getWindow().addFlags(flags)
			videoView.setVideoSize(width, height);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			btn_screen
					.setBackgroundResource(R.drawable.btn_full_screen_selector);
			isFullScreen = false;
			break;
		}
	}
}
// 获取手机宽高上面的像素点
// DisplayMetrics displayMetrics = new DisplayMetrics();
// getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
// int heightPixels = displayMetrics.heightPixels;
// int widthPixels = displayMetrics.widthPixels;