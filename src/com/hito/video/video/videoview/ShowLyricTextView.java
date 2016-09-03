package com.hito.video.video.videoview;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.hito.video.utils.LyricUtils;
import com.hito.video.video.domain.Lyric;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 
 * ��ʾ���
 * 
 * @author Hito 1.���� 2.���� 3.onLayout(ViewGroup����ʵ��) 4.onDraw
 */
public class ShowLyricTextView extends TextView {

	private Paint currentPaint;
	private Paint noCurrentPaint;
	private List<Lyric> lyrics;

	private LyricUtils lyricUtils;
	/**
	 * ��ǰ���ʵ�λ��
	 */
	private int index;

	/**
	 * �����ļ�ʵ������ʱ�����
	 * 
	 * @param context
	 * @param attrs
	 */
	public ShowLyricTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		currentPaint = new Paint();
		// ���û��ʵ���ɫΪ��ɫ
		currentPaint.setColor(Color.GREEN);
		currentPaint.setAntiAlias(true);
		// ���þ���
		currentPaint.setTextAlign(Paint.Align.CENTER);
		// �������ִ�С
		currentPaint.setTextSize(18);
		// ���ǵ�ǰ��Ļ���
		noCurrentPaint = new Paint();
		// ���û��ʵ���ɫΪ��ɫ
		noCurrentPaint.setColor(Color.WHITE);
		noCurrentPaint.setAntiAlias(true);
		// ���þ���
		noCurrentPaint.setTextAlign(Paint.Align.CENTER);
		// �������ִ�С
		noCurrentPaint.setTextSize(16);

		lyrics = new ArrayList<Lyric>();
		lyricUtils = new LyricUtils();

		// // ��Ӽ�����
		// for (int i = 0; i < 200; i++) {
		// Lyric lyric = new Lyric();
		// lyric.setContent(i + "wwwwwwwwwwwww" + i);
		// lyric.setTimePoint(1000 * i);
		// lyric.setSleepTime(2000);
		// lyrics.add(lyric);
		// }

		try {
			// copyLyricToSDCard();
			
			getLyric();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �õ����
	 * 
	 * @throws IOException
	 */
	private void getLyric() throws Exception {

		InputStream is = getResources().getAssets().open("gaobaiqiqiu.txt");
		// BufferedReader bufReader = new BufferedReader(reader);
		// String line="";
		// String Result="";
		// while((line = bufReader.readLine()) != null)
		// Result += line;
		// return Result;
		lyricUtils.readLyricFile(is);
		lyrics = lyricUtils.getLyrics();

	}

	/**
	 * ��ǰ�ؼ��Ŀ�
	 */
	private int width;
	/**
	 * ��ǰ�ؼ��ĸ�
	 */
	private int height;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	/**
	 * ÿһ�и�ʵĸ߶�
	 */
	private float textHeight = 20;
	private int currentPosition;
	/**
	 * ʱ���
	 */
	private long timePoint;
	/**
	 * �þ��ʸ�����ʾ��ʱ��
	 */
	private long sleepTime;

	/**
	 * ���ݽ��ȼ������ʾ�ľ���
	 * 
	 * @param currentPosition
	 */
	public void setShowNextLyric(int currentPosition) {
		this.currentPosition = currentPosition;
		if (lyrics == null) {
			return;
		}
		// �ҳ��ľ�ø�����ʾ
		for (int i = 1; i < lyrics.size(); i++) {
			if (currentPosition < lyrics.get(i).getTimePoint()) {
				int tempindex = i - 1;// �����˵�0��
				// �����ҳ����ϸ�����ʾ���Ǿ���,�õ���ʵ�λ�ã���ʵ�ʱ�������ʵĸ�����ʾʱ��
				if (currentPosition >= lyrics.get(tempindex).getTimePoint()) {
					index = tempindex;
					timePoint = lyrics.get(tempindex).getTimePoint();
					sleepTime = lyrics.get(tempindex).getSleepTime();
				}
			}
		}

		// ����onDrawִ�еĵ���
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// ��Y����ƽ�Ƶ�����
		float plus = 0;
		if (sleepTime == 0) {
			plus = 0;
		} else {
			// ƽ�Ƶ����� = �и� + �ƶ����еİٷ�֮���ٵľ���
			// �ƶ����еİٷ�֮���ٵľ��� = �ƶ����ٶ�*�еĸ߶�
			float datel = ((currentPosition - timePoint) / sleepTime)
					* textHeight;
			plus = textHeight + datel;
			canvas.translate(0, -plus);
		}

		// 1. ����ǰ����
		if (lyrics != null && lyrics.size() > 0) {
			String content = lyrics.get(index).getContent();
			canvas.drawText(content, width / 2, height / 2, currentPaint);
			// 2.����ǰ��֮ǰ�ĸ��
			float tempY = height / 2;
			for (int i = index - 1; i > 0; i--) {
				String nextContent = lyrics.get(i).getContent();
				tempY = tempY - textHeight;
				canvas.drawText(nextContent, width / 2, tempY, noCurrentPaint);
				if (tempY < 0) {
					break;
				}
			}
			// 3.����ǰ��֮��ĸ��
			tempY = height / 2;
			for (int i = index + 1; i < lyrics.size(); i++) {
				String pretContent = lyrics.get(i).getContent();
				tempY = tempY + textHeight;
				canvas.drawText(pretContent, width / 2, tempY, noCurrentPaint);
				if (tempY > height) {
					break;
				}
			}

		} else {
			canvas.drawText("û���ҵ����", width / 2, height / 2, currentPaint);
		}

		// canvas.drawText("�����ʾ", getWidth() / 2, getHeight() / 2, paint);
	}

}
