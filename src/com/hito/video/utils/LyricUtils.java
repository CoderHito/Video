package com.hito.video.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.hito.video.video.domain.Lyric;

public class LyricUtils {

	private ArrayList<Lyric> lyrics;

	public List<Lyric> getLyrics() {
		return lyrics;
	}

	/**
	 * 根据传入的歌词文件解析歌词
	 * 
	 * @param is
	 * @throws Exception
	 */
	public void readLyricFile(InputStream is) throws Exception {

		if (is == null) {
			// 歌词文件不存在
			// || file.exists()
		} else {
			// 解析歌词
			lyrics = new ArrayList<Lyric>();
			// 1.读取文件
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis,
					"GBK"));
			String line;// 一行的内容
			while ((line = br.readLine()) != null) {
				// 2.解析歌词
				line = analyzeLyric(line);
				System.out.println(line);
			}
			// 3.歌词时间排序
			// 4.计算每一句歌词高亮显示的时间
		}
	}

	/**
	 * 解析歌词
	 * 
	 * @param line
	 */
	private String analyzeLyric(String line) {
		// 得到左边第一个括号的位置，和右边第一个括号的位置
		int pos1 = line.indexOf("[");// 0 如果没有 返回-1
		int pos2 = line.indexOf("]");
		if (pos1 == 0 && pos2 != -1) {
			// 定义long类型的数组 装 时间戳
			long[] timePoints = new long[getTagCount(line)];
			// 得到时间戳
			String contentStr = line.substring(pos1 + 1, pos2);
			timePoints[0] = timeStrToLong(contentStr);
			if (timePoints[0] == -1) {
				return "";
			}
			String content = line;
			int i = 1;
			// 当这个while循环结束的时候，时间戳都得到了
			while (pos1 == 0 && pos2 != -1) {
				content = content.substring(pos2 + 1);
				pos1 = content.indexOf("[");// 0 如果没有 返回-1
				pos2 = content.indexOf("]");

				if (pos2 != -1) {
					contentStr = content.substring(pos1 + 1, pos2);
					timePoints[i] = timeStrToLong(content);

					if (timePoints[i] == -1) {
						return "";
					}
					i++;
				}
			}
			Lyric lyric = new Lyric();
			for (int j = 0; j < timePoints.length; j++) {
				if (timePoints[j] != 0) {
					// 歌词内存容
					lyric.setContent(content);
					// 时间戳
					lyric.setTimePoint(timePoints[j]);
					lyrics.add(lyric);
					lyric = new Lyric();
				}
			}
			return content;
		}
		return "";
	}

	/**
	 * 把时间戳转换成毫秒
	 * 
	 * @param content
	 * @return
	 */
	private long timeStrToLong(String content) {
		long result = 0;
		try {
			// 假设content是02:04.12
			// 切割成02和04.12
			// 04.12切割成04 12
			String[] s1 = content.split(":");
			String[] s2 = s1[1].split("\\.");

			// 分
			long min = Long.valueOf(s1[0]);
			// 秒
			long second = Long.valueOf(s2[0]);
			// 毫秒
			long mil = Long.valueOf(s2[1]);
			result = min * 60 * 1000 + second * 1000 + min * 10;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return result;
	}

	/**
	 * 判断有多少句歌词，至少要返回1
	 * 
	 * @return
	 */
	private int getTagCount(String line) {
		String[] left = line.split("\\[");
		String[] right = line.split("\\]");
		if (left.length == 0 || right.length == 0) {
			return 1;
		} else if (left.length > right.length) {
			return left.length;
		} else {
			return right.length;
		}
	}
}
