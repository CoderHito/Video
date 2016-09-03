package com.hito.video.video.domain;

import java.io.Serializable;

/**
 * 一个具体的视频
 * 
 * intent传递数据需要序列化
 * 
 * @author hito
 */
public class VideoItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 标题、时长、文件大小、播放地址
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
