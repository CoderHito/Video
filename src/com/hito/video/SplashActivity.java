package com.hito.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class SplashActivity extends Activity {
	/**
	 * true �Ѿ�������ҳ��
	 * 
	 * false ��û������ҳ��
	 */
	private boolean isEnterMain = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// �ȴ������ٽ�����ҳ��
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				enterMain();
			}

		}, 2000);
	}

	/**
	 * ������ҳ��
	 */
	protected void enterMain() {
		if (!isEnterMain) {
			isEnterMain = true;
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		enterMain();
		return super.onTouchEvent(event);
	}
}
