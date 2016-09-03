package com.hito.video.video.domain;

/**
 * 代表一句歌词
 * 
 * @author Hito
 * 
 */
public class Lyric {

	/**
	 * 歌词内容
	 */
	private String content;
	/**
	 * 时间戳
	 */
	private long timePoint;
	/**
	 * 歌词显示时间
	 */
	private long sleepTime;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getTimePoint() {
		return timePoint;
	}
	public void setTimePoint(long timePoint) {
		this.timePoint = timePoint;
	}
	public long getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	

}
