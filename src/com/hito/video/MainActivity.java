package com.hito.video;

import com.hito.video.audio.AudioListActivity;
import com.hito.video.video.VideoListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	private GridView gridView;
	// 图片资源id
	private int[] ids = { R.drawable.channel_main_film,
			R.drawable.channel_main_tv, R.drawable.channel_main_film,
			R.drawable.channel_main_tv, R.drawable.channel_main_film,
			R.drawable.channel_main_tv };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 隐藏左边按钮
		setLeftButton(View.GONE);
		setTitle("手机影音");

		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new MyMainAdapter());
		// 设置点击事件
		gridView.setOnItemClickListener(new OnItemClickListener() {

			private Intent intent;

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					intent = new Intent(MainActivity.this,
							VideoListActivity.class);
					startActivity(intent);

					break;
				case 1:
					intent = new Intent(MainActivity.this,
							AudioListActivity.class);
					startActivity(intent);
					break;
				// case 2:
				// break;
				// case 3:
				// break;
				// case 4:
				// break;
				// case 5:
				// break;

				default:
					Toast.makeText(MainActivity.this, "该模块未启用", 0).show();
					break;
				}
			}
		});

	}

	static class ViewHolder {
		ImageView iv_icon;
	}

	private class MyMainAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ids.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View
						.inflate(MainActivity.this, R.layout.main_item, null);
				holder = new ViewHolder();// 容器
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);

				// 容器和view的对应关系
				view.setTag(holder);
			}
			holder.iv_icon.setImageResource(ids[position]);
			return view;
		}

		@Override
		public Object getItem(int position) {
			return ids[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	@Override
	public void rightButtonClick() {
		Toast.makeText(this, "右边", 0).show();
	}

	@Override
	public void leftButtonClick() {
		Toast.makeText(this, "左边", 0).show();
	}

	@Override
	public View setChildContentView() {
		// 将布局文件转换为View对象
		return View.inflate(this, R.layout.activity_main, null);
	}

}
