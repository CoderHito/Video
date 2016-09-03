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
 * ��Ƶ������--��ϵͳapiд�� ---����������ϵͳ�Դ��Ĳ�����һ���Ĺ���
 * 
 * @author hito
 * 
 */
public class VideoPlayActivity extends BaseActivity {

	/**
	 * ���½���
	 */
	protected static final int PROGRESS = 1;
	/**
	 * ���ؿ������
	 */
	protected static final int DELAYED_HIDE_CONTROL_PLAYER = 2;
	/**
	 * ȫ����ʾ
	 */
	private static final int FULL_SCREEN = 3;

	/**
	 * Ĭ����Ļ
	 */
	private static final int DEFAULT_SCREEN = 4;
	/**
	 * �ӳ�����رյ�ǰ������
	 */
	private static final int FINISH = 5;

	/**
	 * �Ƿ���ȫ��
	 * 
	 * true��ȫ��
	 * 
	 * false��Ĭ��
	 */
	private boolean isFullScreen = false;
	private VideoView videoView;
	private Uri uri;// ��Ƶ��ַ

	private TextView tv_video_title, tv_system_time, tv_current_time,
			tv_duration;
	private ImageView iv_battery;
	private Button btn_voice, btn_switch, btn_exit, btn_pre, btn_play_pause,
			btn_next, btn_screen;
	private SeekBar seekbar_voice, seekbar_video;
	private Utils utils;
	private MyBroadcastReceiver receiver;
	/**
	 * ��Ƶ�б�
	 */
	private ArrayList<VideoItem> videoItems;
	/**
	 * ��Ƶ��ǰλ�ã�Ҫ���ŵ���Ƶ��λ��
	 */
	private int position;
	/**
	 * ϵͳ������Χֵ0-100
	 */
	private int level;
	/**
	 * �ж��Ƿ��ǲ���״̬ true��Ϊ����״̬ false��Ϊ��ͣ״̬
	 */
	private boolean isPlay = false;
	/**
	 * �жϵ�ǰActivity�Ƿ����� true���Ѿ������� false��û�б�����
	 */
	private boolean isDestory = false;

	private GestureDetector detector;
	private LinearLayout ll_control_player;

	/**
	 * �Ƿ���ʾ�������
	 * 
	 * true�� ��ʾ false������
	 */
	private boolean isShowControl = false;

	private WindowManager wm;
	/**
	 * ��Ļ�Ŀ�
	 */
	private int screenWidth;
	/**
	 * ��Ļ�ĸ�
	 */
	private int screenHeight;

	/**
	 * ����������С
	 */
	private AudioManager am;

	/**
	 * ��ǰ����
	 */
	private int currentVolume;
	/**
	 * �������
	 */
	private int maxVolume;

	/**
	 * ��ǰ�Ƿ��Ǿ���
	 * 
	 * true:�Ǿ���
	 * 
	 * false:�Ǿ���
	 */
	private boolean isMute = false;

	/**
	 * ��Ƶ����
	 */
	private LinearLayout ll_loading;
	/**
	 * ��Ƶ����
	 */
	private LinearLayout ll_buffering;
	/**
	 * true�����˵��ǻ�û�аѿ�Ч������
	 * 
	 * false�����˵��ǰѿ���Ч��������
	 * 
	 */
	private boolean isBuffing = false;
	/**
	 * ��ָ����Ļ�ϻ�������ʼλ��
	 */
	private float startY;
	/**
	 * ��Ļ�����ķ�Χ
	 */
	private float audioTouchRang;
	/**
	 * ����ǰ������ֵ
	 */
	private int mVol;
	/**
	 * �жϵ�ǰ����·���Ƿ���Դ������
	 */
	private boolean isNetUri;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				// �õ���Ƶ��ǰ���Ž���
				int currentPosition = videoView.getCurrentPosition();
				tv_current_time.setText(utils.stringFroTime(currentPosition));
				// 2,seekbar���½���
				seekbar_video.setProgress(currentPosition);

				// ���õ�������ʾ
				setBattery();
				// ������ʾ��ǰ�ֻ���ʱ��
				tv_system_time.setText(utils.getSystemTime());
				// �ж��Ƿ�����Դ������,������ػ�����ȣ������򲻼��ػ������
				if (isNetUri) {
					// ���û������
					// �������ֵ�� 0 - 100 ֮��
					int percentage = videoView.getBufferPercentage();
					int total = percentage * seekbar_video.getMax();
					int secondaryProgress = total / 100;
					seekbar_video.setSecondaryProgress(secondaryProgress);
				} else {
					seekbar_video.setSecondaryProgress(0);
				}

				// ��Ϣ����ѭ��
				if (!isDestory) {
					mHandler.removeMessages(PROGRESS);
					mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
				}
				break;
			case DELAYED_HIDE_CONTROL_PLAYER:
				hideControlPlayer();
				break;
			case FINISH:// �رյ�ǰҳ��
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
		// ��ӿ�����
		// videoView.setMediaController(new MediaController(this));
	}

	/**
	 * ��������
	 */
	private void setData() {
		if (videoItems != null && videoItems.size() > 0) {
			VideoItem videoItem = videoItems.get(position);
			videoView.setVideoPath(videoItem.getData());
			isNetUri = utils.isNetUri(videoItems.toString());
			tv_video_title.setText(videoItem.getTitle());
		} else if (uri != null) {
			// ���ò��ŵ�ַ
			videoView.setVideoURI(uri);
			// ���ñ���
			tv_video_title.setText(uri.toString());
			isNetUri = utils.isNetUri(uri.toString());
			btn_pre.setBackgroundResource(R.drawable.btn_pre_gray);
			btn_pre.setEnabled(false);

			btn_next.setBackgroundResource(R.drawable.btn_next_gray);
			btn_next.setEnabled(false);
		}
		// seekbar�������ܴ�С���й���
		seekbar_voice.setMax(maxVolume);
		seekbar_voice.setProgress(currentVolume);
		Log.e("volume", currentVolume + "");
	}

	/**
	 * �õ�����
	 */
	private void getData() {
		// �õ������б�
		Intent intent = getIntent();
		videoItems = (ArrayList<VideoItem>) intent
				.getSerializableExtra("videolist");
		intent.getIntExtra("position", 0);

		// �õ����ŵ�ַ һ���ǵ��������
		uri = getIntent().getData();
		// videoView.setVideoURI(uri);
	}

	@SuppressWarnings("deprecation")
	private void initData() {
		isDestory = false;
		utils = new Utils();

		// ���õ�������Ƶ��ʱ������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// �õ���Ļ �ߺͿ�
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		// ���������仯
		receiver = new MyBroadcastReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);// �����仯��ʱ��ϵͳ�ᷢ������㲥
		registerReceiver(receiver, filter);
		// ʵ��������ʶ����
		detector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					/**
					 * �����¼�
					 */
					@Override
					public void onLongPress(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "������Ļ", 0)
						// .show();
						super.onLongPress(e);
						startOrPause();
					}

					/**
					 * ˫���¼�
					 */
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						// Toast.makeText(getApplicationContext(), "˫����Ļ", 0)
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
					 * �����¼�
					 */
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "������Ļ", 0)
						// .show();
						super.onSingleTapConfirmed(e);
						if (isShowControl) {
							// ���֮ǰ�Ѿ������ˣ����Ƴ�֮ǰ����Ϣ
							removeDelayedHideControlPlayer();
							hideControlPlayer();

						} else {
							showControlPlayer();
							// ������Ϣ���ؿ������
							SendDelayedHideControlPlayer();
						}
						return true;
					}

				});

		// �õ���ǰ�������������ֵ
		am = (AudioManager) getSystemService(AUDIO_SERVICE);
		// �õ���ǰ������С
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		// �������ֵ ��Χ0-15֮��
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

	}

	/**
	 * ������Ϣ
	 * 
	 * �ӳ�5s���ؿ������
	 */
	private void SendDelayedHideControlPlayer() {
		mHandler.sendEmptyMessageDelayed(DELAYED_HIDE_CONTROL_PLAYER, 5000);
	}

	/**
	 * �Ƴ��ӳ����ؿ���������Ϣ
	 */
	protected void removeDelayedHideControlPlayer() {
		mHandler.removeMessages(DELAYED_HIDE_CONTROL_PLAYER);
	}

	/**
	 * ʹ������ʶ����
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// ��ָ������Ļ
			removeDelayedHideControlPlayer();
			// ��¼��ʼֵ
			startY = event.getY();
			audioTouchRang = Math.min(screenHeight, screenWidth);
			mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			break;
		case MotionEvent.ACTION_MOVE:
			/**
			 * ��¼endY
			 */
			float endY = event.getY();
			/**
			 * ����ƫ����
			 */
			float distanceY = startY - endY;
			/**
			 * ������Ļ��������
			 */
			float detal = distanceY / audioTouchRang;
			/**
			 * ����ı������ֵ
			 * 
			 * �ı������ = �����ľ���/�ܾ���*������
			 */
			float volume = distanceY / audioTouchRang * maxVolume;
			/**
			 * ���ηǷ�ֵ �ҳ�Ҫ���õ�����ֵ
			 */
			float volmeS = Math.min(Math.max(volume + mVol, 0), maxVolume);

			if (detal != 0) {
				updateVoice((int) volmeS);
			}

			break;
		case MotionEvent.ACTION_UP:// ��ָ�뿪
			SendDelayedHideControlPlayer();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * ���õ����ı仯
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
			// �õ�������ֵ 0-100
			level = intent.getIntExtra("level", 0);
		}

	}

	protected void onDestroy() {
		super.onDestroy();
		isDestory = true;
		// ȡ����������
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
			case R.id.btn_play_pause:// ���ź���ͣ���л�
				// ���Ż�����ͣ
				startOrPause();
				break;
			case R.id.btn_next:// ��һ��
				playNextVideo();
				break;
			case R.id.btn_pre:// ��һ��
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
				// ���þ����ͷǾ���
				isMute = !isMute;
				updateVoice(currentVolume);
				break;
			case R.id.btn_switch:
				new AlertDialog.Builder(VideoPlayActivity.this)
						.setMessage("��ǰ��ϵͳ���������Ƿ��л������ܲ�����")
						.setNegativeButton("�´���˵", null)
						.setPositiveButton("�����л�",
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
	 * ���ø��ּ���
	 */
	private void setListener() {

		// ���ð�ť�ļ���
		btn_play_pause.setOnClickListener(mOnClickListener);

		btn_next.setOnClickListener(mOnClickListener);
		btn_pre.setOnClickListener(mOnClickListener);

		btn_screen.setOnClickListener(mOnClickListener);
		btn_voice.setOnClickListener(mOnClickListener);
		btn_switch.setOnClickListener(mOnClickListener);
		btn_exit.setOnClickListener(mOnClickListener);
		// ����seekbar״̬�ı�ļ���������������С
		seekbar_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// ��ʼ������ʱ��ȡ�����ؽ������Ϣ
				removeDelayedHideControlPlayer();
				Log.e("volume", "��ʼ");
			}

			/**
			 * ����������С
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
		// ��Ƶ����
		seekbar_video.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			/**
			 * ��ָ�뿪seekbar��ʱ��ص��ķ���
			 */
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				SendDelayedHideControlPlayer();
			}

			/**
			 * seekbar���϶���ʱ��ص��ķ���
			 * */
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				removeDelayedHideControlPlayer();
			}

			/**
			 * ��seekbar״̬�����ı��ʱ��ص��������
			 * 
			 * seekbar:����
			 * 
			 * progress:seekbar��λ�ã���Ƶ�ĳ��Ⱥ�seekbar����һһ��Ӧ
			 * 
			 * fromUser��seekbar������ʱ��Ϊtrue
			 * */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					// �϶��������λ��
					videoView.seekTo(progress);
				}
			}
		});

		// ������Ƶ�Ƿ�׼������--��ʼ����
		videoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// ��ʼ����
				videoView.start();
				isPlay = true;
				// �õ���Ƶ����
				int duration = videoView.getDuration();
				tv_duration.setText(utils.stringFroTime(duration));

				// 1,��Ƶ��ʱ�� ����seekbar
				seekbar_video.setMax(duration);

				// ������Ļ��СΪĬ��
				setVideoType(DEFAULT_SCREEN);

				// �������ؿ������
				hideControlPlayer();
				// ���ؼ���Ч��
				ll_loading.setVisibility(View.GONE);
				// ���½���
				mHandler.sendEmptyMessage(PROGRESS);
			}
		});

		// ���ò�����ɼ���
		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playNextVideo();
			}
		});

		// ���ü����Ƿ񲥷ų�����
		videoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				/**
				 * ��Ƶ���ų����ԭ������Щ��
				 * 
				 * ���ŵ���Ƶ��ʽ��֧��---�������ܲ���������
				 * 
				 * ����������Ƶ���������---�������²��ŵķ�ʽ
				 * 
				 * ���ص���Ƶ�ļ����������пհ�---�������⣬�޸����ص�bug
				 * 
				 * ����һ��������Ƶ���ļ�---��ʾ�û�
				 * 
				 * ֻҪ�����ˣ�����ص��������
				 */
				// Toast.makeText(getApplicationContext(), "���ų�����...",
				// 0).show();
				// ���ų����������ܲ�����
				startVitamioPlayer();
				return true;
			}
		});

		// �������ſ���,������ʾ�û�Android2.3�Ժ���е�
		videoView.setOnInfoListener(new OnInfoListener() {

			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:// ���� ���� �϶�����
					// Toast.makeText(getApplicationContext(), "����", 1).show();
					ll_buffering.setVisibility(View.VISIBLE);
					isBuffing = true;
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:// ������ ���� �϶��� ����
					// Toast.makeText(getApplicationContext(), "������", 1).show();
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
		 * ���ü����϶����
		 * 
		 * ����bug���Ѿ����ٽ������ǽ�����û������
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
	 * �������ܲ�����
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
		// �رյ�ǰ��Activity-������---
		// finish();
		// ���̹رջᵼ��ϵͳ���������±�����---�ӳ��������ϵͳ������
		mHandler.sendEmptyMessageDelayed(FINISH, 2000);
	}

	/**
	 * ���������ķ���
	 * 
	 * @param volume
	 *            ��Ҫ���ڳɵ�����ֵ
	 */
	protected void updateVoice(int volume) {
		if (isMute) {
			// ����
			// ���һ�������ĳ�1�Ļ��������϶����ĸı䣬ϵͳ������Ҳ�ᷢ���ı�
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			seekbar_voice.setProgress(0);
		} else {
			// �Ǿ���
			am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
			seekbar_voice.setProgress(volume);
		}
		currentVolume = volume;
	}

	/**
	 * ��ʼ��View
	 */
	private void initView() {
		setTitleBar(View.GONE);// ���ر�����
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
	 * ���ؿ������
	 */
	private void hideControlPlayer() {
		ll_control_player.setVisibility(View.GONE);
		isShowControl = false;
	}

	/**
	 * ��ʾ�������
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
	 * ���ð�ť��״̬
	 */
	private void setPlayOrPauseStatus() {
		// ��������һ����Ƶ����һ����ť�Ͳ����Ե�������ұ��
		if (position == 0) {
			// ��һ����Ƶ,��һ�����ɵ�
			btn_pre.setBackgroundResource(R.drawable.video_pre_gray);
			btn_pre.setEnabled(false);
		} else if (position == videoItems.size() - 1) {
			// ���һ����Ƶ
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
	 * ������һ����Ƶ
	 */
	private void playNextVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position++;// ��һ����Ƶ
			if (position < videoItems.size()) {
				VideoItem videoItem = videoItems.get(position);
				videoView.setVideoPath(videoItem.getData());
				isNetUri = utils.isNetUri(videoItem.getData().toString());
				// ���ñ���
				tv_video_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {

				position = videoItems.size() - 1;
				Toast.makeText(getApplicationContext(), "���һ����Ƶ", 0).show();
				finish();// �˳�������
			}
		} else if (uri != null) {
			Toast.makeText(getApplicationContext(), "�������", 0).show();
			finish();// �˳�������
		}
	}

	/**
	 * ������һ����Ƶ
	 */
	private void playPreVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position--;// ��һ����Ƶ
			if (position >= 0) {
				VideoItem videoItem = videoItems.get(position);
				isNetUri = utils.isNetUri(videoItem.getData().toString());
				videoView.setVideoPath(videoItem.getData());
				// ���ñ���
				tv_video_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {

				position = 0;
				Toast.makeText(getApplicationContext(), "�Ѿ��ǵ�һ����Ƶ", 0).show();
			}

		}
	}

	/**
	 * ���Ż�����ͣ
	 */
	private void startOrPause() {
		if (isPlay) {
			// ��ͣ
			videoView.pause();
			// ��ť״̬Ҫ����Ϊ����
			btn_play_pause.setBackgroundResource(R.drawable.btn_play_selector);
		} else {
			// ����
			// ��ť״̬Ҫ����Ϊ��ͣ
			videoView.start();
			btn_play_pause.setBackgroundResource(R.drawable.btn_pause_selector);
		}
		isPlay = !isPlay;
	}

	/**
	 * ������Ƶ�����ͣ�ȫ����Ĭ��
	 * 
	 * @param type
	 */
	public void setVideoType(int type) {
		switch (type) {
		case FULL_SCREEN:// ȫ��

			videoView.setVideoSize(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = true;
			btn_screen
					.setBackgroundResource(R.drawable.btn_default_screen_selector);
			break;
		case DEFAULT_SCREEN:// Ĭ��
			int mVideoHeight = videoView.getVideoHeight();
			int mVideoWidth = videoView.getVideoWidth();
			// �������Ƶ�����õĿ��
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
// ��ȡ�ֻ������������ص�
// DisplayMetrics displayMetrics = new DisplayMetrics();
// getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
// int heightPixels = displayMetrics.heightPixels;
// int widthPixels = displayMetrics.widthPixels;