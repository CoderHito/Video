package com.hito.video.audio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hito.video.BaseActivity;
import com.hito.video.R;
import com.hito.video.utils.Utils;
import com.hito.video.video.domain.AudioItem;

public class AudioListActivity extends BaseActivity {

	private ListView lv_audioList;
	private TextView tv_noaudio;
	private ArrayList<AudioItem> audioItems;
	private Utils utils;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (audioItems != null && audioItems.size() > 0) {
				tv_noaudio.setVisibility(View.GONE);
				lv_audioList.setAdapter(new VideoListAdapter());
			} else {
				tv_noaudio.setVisibility(View.VISIBLE);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		// 加载视频数据
		getAllAudio();
		setListener();
	}

	/**
	 * 设置点击事件
	 */
	public void setListener() {
		lv_audioList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 根据点击的位置取出对应的音乐信息
				AudioItem item = audioItems.get(position);
				// Toast.makeText(VideoListActivity.this, item.getTitle(),
				// 0).show();
				// Intent intent = new Intent(AudioListActivity.this,
				// AudioPlayerActivity.class);
				// intent.setAction(Intent.ACTION_VIEW);
				// intent.setDataAndType(Uri.parse(item.getData()), "audio/*");
				// intent.setData(Uri.parse(item.getData()));
				// startActivity(intent);

				// 传入播放列表和当前点击的位置
				Intent intent = new Intent(AudioListActivity.this,
						AudioPlayerActivity.class);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化View
	 */
	public void initView() {
		// 设置标题
		setTitle("本地音乐");
		// 隐藏右边按钮
		setRightButton(View.GONE);

		lv_audioList = (ListView) findViewById(R.id.lv_audiolist);
		tv_noaudio = (TextView) findViewById(R.id.tv_noaudio);
		utils = new Utils();
	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_duration;
		TextView tv_size;
	}

	private class VideoListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return audioItems.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(AudioListActivity.this,
						R.layout.audiolist_item, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_duration = (TextView) view
						.findViewById(R.id.tv_duration);

				holder.tv_size = (TextView) view.findViewById(R.id.tv_size);

				// 对应关系保存起来
				view.setTag(holder);
			}
			// 根据位置得到具体某一条视频的信息
			AudioItem videoItem = audioItems.get(position);
			holder.tv_name.setText(videoItem.getTitle());

			holder.tv_duration.setText(utils.stringFroTime(Integer
					.valueOf(videoItem.getDuration())));
			holder.tv_size.setText(Formatter.formatFileSize(
					AudioListActivity.this, videoItem.getSize()) + "");
			return view;
		}

		@Override
		public Object getItem(int position) {
			return audioItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

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
				mHandler.sendEmptyMessage(0);
			}
		}).start();
	}

	@Override
	public View setChildContentView() {
		return View.inflate(this, R.layout.activity_audio_list, null);
	}

	@Override
	public void rightButtonClick() {

	}

	@Override
	public void leftButtonClick() {
		finish();
	}

}
