package com.hito.video.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class Utils {
	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;

	public Utils() {
		// 转换成字符串时间
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
	}

	/**
	 * 把毫秒转换成 时分秒的形式
	 * 
	 * @param timeMS
	 * @return
	 */
	public String stringFroTime(int timeMS) {
		int totalSeconds = timeMS / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;
		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
					.toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	/**
	 * 得到当前系统时间
	 * 
	 * @return
	 */
	public String getSystemTime() {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		return format.format(new Date());
	}

	/**
	 * 判断播放地址是否是网络资源
	 * 
	 * @param path
	 * @return
	 */
	public boolean isNetUri(String path) {
		boolean result = false;
		if (path != null && path.contains("http") || path.contains("rtsp")
				|| path.contains("MMS")) {
			result = true;
		}
		return result;
	}

}
