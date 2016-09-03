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
 * 显示歌词
 * 
 * @author Hito 1.构造 2.测量 3.onLayout(ViewGroup必须实现) 4.onDraw
 */
public class ShowLyricTextView extends TextView {

	private Paint currentPaint;
	private Paint noCurrentPaint;
	private List<Lyric> lyrics;

	private LyricUtils lyricUtils;
	/**
	 * 当前句歌词的位置
	 */
	private int index;

	/**
	 * 布局文件实例化的时候调用
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
		// 设置画笔的颜色为绿色
		currentPaint.setColor(Color.GREEN);
		currentPaint.setAntiAlias(true);
		// 设置居中
		currentPaint.setTextAlign(Paint.Align.CENTER);
		// 设置文字大小
		currentPaint.setTextSize(18);
		// 不是当前句的画笔
		noCurrentPaint = new Paint();
		// 设置画笔的颜色为白色
		noCurrentPaint.setColor(Color.WHITE);
		noCurrentPaint.setAntiAlias(true);
		// 设置居中
		noCurrentPaint.setTextAlign(Paint.Align.CENTER);
		// 设置文字大小
		noCurrentPaint.setTextSize(16);

		lyrics = new ArrayList<Lyric>();
		lyricUtils = new LyricUtils();

		// // 添加假设歌词
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
	 * 得到歌词
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
	 * 当前控件的宽
	 */
	private int width;
	/**
	 * 当前控件的高
	 */
	private int height;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	/**
	 * 每一行歌词的高度
	 */
	private float textHeight = 20;
	private int currentPosition;
	/**
	 * 时间戳
	 */
	private long timePoint;
	/**
	 * 该句歌词高亮显示的时间
	 */
	private long sleepTime;

	/**
	 * 根据进度计算该显示哪句歌词
	 * 
	 * @param currentPosition
	 */
	public void setShowNextLyric(int currentPosition) {
		this.currentPosition = currentPosition;
		if (lyrics == null) {
			return;
		}
		// 找出哪句该高亮显示
		for (int i = 1; i < lyrics.size(); i++) {
			if (currentPosition < lyrics.get(i).getTimePoint()) {
				int tempindex = i - 1;// 包含了第0句
				// 立刻找出符合高亮显示的那句歌词,得到歌词的位置，歌词的时间戳，歌词的高亮显示时间
				if (currentPosition >= lyrics.get(tempindex).getTimePoint()) {
					index = tempindex;
					timePoint = lyrics.get(tempindex).getTimePoint();
					sleepTime = lyrics.get(tempindex).getSleepTime();
				}
			}
		}

		// 导致onDraw执行的调用
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 在Y轴上平移的坐标
		float plus = 0;
		if (sleepTime == 0) {
			plus = 0;
		} else {
			// 平移的坐标 = 行高 + 移动这行的百分之多少的距离
			// 移动这行的百分之多少的距离 = 移动的速度*行的高度
			float datel = ((currentPosition - timePoint) / sleepTime)
					* textHeight;
			plus = textHeight + datel;
			canvas.translate(0, -plus);
		}

		// 1. 画当前句歌词
		if (lyrics != null && lyrics.size() > 0) {
			String content = lyrics.get(index).getContent();
			canvas.drawText(content, width / 2, height / 2, currentPaint);
			// 2.画当前句之前的歌词
			float tempY = height / 2;
			for (int i = index - 1; i > 0; i--) {
				String nextContent = lyrics.get(i).getContent();
				tempY = tempY - textHeight;
				canvas.drawText(nextContent, width / 2, tempY, noCurrentPaint);
				if (tempY < 0) {
					break;
				}
			}
			// 3.画当前句之后的歌词
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
			canvas.drawText("没有找到歌词", width / 2, height / 2, currentPaint);
		}

		// canvas.drawText("歌词显示", getWidth() / 2, getHeight() / 2, paint);
	}

}
