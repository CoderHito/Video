package com.hito.video;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class BaseActivity extends Activity {

	private Button btn_left, btn_right;
	private TextView tv_title;
	private LinearLayout ll_child_content;
	private FrameLayout fl_titlebar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 用代码设置隐藏标题 所有继承这个类的都没有标题
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_base);

		initView();
		setOnclickListener();
	}

	/**
	 * 设置事件
	 */
	private void setOnclickListener() {

		btn_left.setOnClickListener(mOnclickListener);
		btn_right.setOnClickListener(mOnclickListener);
	}

	OnClickListener mOnclickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_left:// 左边按钮
				leftButtonClick();
				break;
			case R.id.btn_right:// 右边按钮
				rightButtonClick();
			default:
				break;
			}
		}
	};

	/**
	 * 初始化view
	 */
	private void initView() {
		btn_left = (Button) findViewById(R.id.btn_left);
		btn_right = (Button) findViewById(R.id.btn_right);
		tv_title = (TextView) findViewById(R.id.tv_title);
		fl_titlebar = (FrameLayout) findViewById(R.id.fl_titlebar);

		ll_child_content = (LinearLayout) findViewById(R.id.ll_child_content);

		View child = setChildContentView();

		if (child != null) {

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			// 添加孩子的布局文件
			ll_child_content.addView(child, params);
		}

	}

	/**
	 * 加载布局文件，由子类实现
	 * 
	 * @return
	 */
	public abstract View setChildContentView();

	/**
	 * 点击右边按钮，由子类具体实现
	 */
	public abstract void rightButtonClick();

	/**
	 * 点击左边按钮，由子类具体实现
	 */
	public abstract void leftButtonClick();

	/**
	 * 设置左边按钮的显示状态
	 */
	public void setLeftButton(int visibility) {
		btn_left.setVisibility(visibility);
	}

	/**
	 * 设置右边按钮的显示状态
	 */
	public void setRightButton(int visibility) {
		btn_right.setVisibility(visibility);
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		tv_title.setText(title);
	}

	/**
	 * 设置标题栏是否隐藏
	 * 
	 * @param visibility
	 */
	public void setTitleBar(int visibility) {
		fl_titlebar.setVisibility(visibility);
	}

}
