package com.hito.video.video.domain;

/**
 * ����һ����
 * 
 * @author Hito
 * 
 */
public class Lyric {

	/**
	 * �������
	 */
	private String content;
	/**
	 * ʱ���
	 */
	private long timePoint;
	/**
	 * �����ʾʱ��
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
