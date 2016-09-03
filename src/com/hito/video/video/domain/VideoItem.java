package com.hito.video.video.domain;

import java.io.Serializable;

/**
 * һ���������Ƶ
 * 
 * intent����������Ҫ���л�
 * 
 * @author hito
 */
public class VideoItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ���⡢ʱ�����ļ���С�����ŵ�ַ
	private String title;
	private String duration;
	private long size;
	private String data;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "VideoItem [title=" + title + ", duration=" + duration
				+ ", size=" + size + ", data=" + data + "]";
	}

}