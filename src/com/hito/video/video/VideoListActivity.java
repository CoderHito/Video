package com.hito.video.video;

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
import com.hito.video.video.domain.VideoItem;

public class VideoListActivity extends BaseActivity {

	private ListView lv_videolist;
	private TextView tv_novideo;
	private ArrayList<VideoItem> videoItems;
	private Utils utils;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (videoItems != null && videoItems.size() > 0) {
				tv_novideo.setVisibility(View.GONE);
				lv_videolist.setAdapter(new VideoListAdapter());
			} else {
				tv_novideo.setVisibility(View.VISIBLE);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ���ñ���
		setTitle("������Ƶ");
		// �����ұ߰�ť
		setRightButton(View.GONE);

		lv_videolist = (ListView) findViewById(R.id.lv_videolist);
		tv_novideo = (TextView) findViewById(R.id.tv_novideo);

		utils = new Utils();
		// ������Ƶ����
		getAllVideo();

		// ���õ���¼�
		lv_videolist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ���ݵ����λ��ȡ����Ӧ����Ƶ��Ϣ
				// VideoItem item = videoItems.get(position);
				// // Toast.makeText(VideoListActivity.this, item.getTitle(),
				// // 0).show();
				// Intent intent = new Intent(VideoListActivity.this,
				// VideoPlayActivity.class);
				// intent.setData(Uri.parse(item.getData()));
				// startActivity(intent);

				// ���벥���б�͵�ǰ�����λ��
				Intent intent = new Intent(VideoListActivity.this,
						VideoPlayActivity.class);
				Bundle extras = new Bundle();
				extras.putSerializable("videolist", videoItems);
				intent.putExtras(extras);
				intent.putExtra("position", position);

				startActivity(intent);
			}
		});

	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_duration;
		TextView tv_size;
	}

	private class VideoListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return videoItems.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(VideoListActivity.this,
						R.layout.videolist_item, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_duration = (TextView) view
						.findViewById(R.id.tv_duration);

				holder.tv_size = (TextView) view.findViewById(R.id.tv_size);

				// ��Ӧ��ϵ��������
				view.setTag(holder);
			}
			// ����λ�õõ�����ĳһ����Ƶ����Ϣ
			VideoItem videoItem = videoItems.get(position);
			holder.tv_name.setText(videoItem.getTitle());

			holder.tv_duration.setText(utils.stringFroTime(Integer
					.valueOf(videoItem.getDuration())));
			holder.tv_size.setText(Formatter.formatFileSize(
					VideoListActivity.this, videoItem.getSize()) + "");
			return view;
		}

		@Override
		public Object getItem(int position) {
			return videoItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	/**
	 * ������Ƶ�������߳�������
	 */
	private void getAllVideo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				videoItems = new ArrayList<VideoItem>();
				// ���ֻ������������Ƶ��Ϣ��ȡ����
				ContentResolver contentResolver = getContentResolver();
				Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				String projection[] = { MediaStore.Video.Media.TITLE,// ����
						MediaStore.Video.Media.DURATION,// ʱ��
						MediaStore.Video.Media.SIZE,// ��Ƶ�ļ��Ĵ�С
						MediaStore.Video.Media.DATA // ��Ƶ��sd���ľ��Ե�ַ-����
				};
				Cursor cursor = contentResolver.query(uri, projection, null,
						null, null);
				while (cursor.moveToNext()) {
					VideoItem item = new VideoItem();

					String title = cursor.getString(0);
					String duration = cursor.getString(1);
					long size = cursor.getLong(2);
					String data = cursor.getString(3);

					item.setTitle(title);
					item.setSize(size);
					item.setData(data);
					item.setDuration(duration);

					videoItems.add(item);

				}
				mHandler.sendEmptyMessage(0);
			}
		}).start();
	}

	@Override
	public View setChildContentView() {
		return View.inflate(this, R.layout.activity_video_list, null);
	}

	@Override
	public void rightButtonClick() {

	}

	@Override
	public void leftButtonClick() {
		finish();
	}

}
