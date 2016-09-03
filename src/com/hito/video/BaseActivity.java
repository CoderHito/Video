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
		// �ô����������ر��� ���м̳������Ķ�û�б���
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_base);

		initView();
		setOnclickListener();
	}

	/**
	 * �����¼�
	 */
	private void setOnclickListener() {

		btn_left.setOnClickListener(mOnclickListener);
		btn_right.setOnClickListener(mOnclickListener);
	}

	OnClickListener mOnclickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_left:// ��߰�ť
				leftButtonClick();
				break;
			case R.id.btn_right:// �ұ߰�ť
				rightButtonClick();
			default:
				break;
			}
		}
	};

	/**
	 * ��ʼ��view
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
			// ��Ӻ��ӵĲ����ļ�
			ll_child_content.addView(child, params);
		}

	}

	/**
	 * ���ز����ļ���������ʵ��
	 * 
	 * @return
	 */
	public abstract View setChildContentView();

	/**
	 * ����ұ߰�ť�����������ʵ��
	 */
	public abstract void rightButtonClick();

	/**
	 * �����߰�ť�����������ʵ��
	 */
	public abstract void leftButtonClick();

	/**
	 * ������߰�ť����ʾ״̬
	 */
	public void setLeftButton(int visibility) {
		btn_left.setVisibility(visibility);
	}

	/**
	 * �����ұ߰�ť����ʾ״̬
	 */
	public void setRightButton(int visibility) {
		btn_right.setVisibility(visibility);
	}

	/**
	 * ���ñ���
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		tv_title.setText(title);
	}

	/**
	 * ���ñ������Ƿ�����
	 * 
	 * @param visibility
	 */
	public void setTitleBar(int visibility) {
		fl_titlebar.setVisibility(visibility);
	}

}
