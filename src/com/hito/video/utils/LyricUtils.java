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
	 * ���ݴ���ĸ���ļ��������
	 * 
	 * @param is
	 * @throws Exception
	 */
	public void readLyricFile(InputStream is) throws Exception {

		if (is == null) {
			// ����ļ�������
			// || file.exists()
		} else {
			// �������
			lyrics = new ArrayList<Lyric>();
			// 1.��ȡ�ļ�
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis,
					"GBK"));
			String line;// һ�е�����
			while ((line = br.readLine()) != null) {
				// 2.�������
				line = analyzeLyric(line);
				System.out.println(line);
			}
			// 3.���ʱ������
			// 4.����ÿһ���ʸ�����ʾ��ʱ��
		}
	}

	/**
	 * �������
	 * 
	 * @param line
	 */
	private String analyzeLyric(String line) {
		// �õ���ߵ�һ�����ŵ�λ�ã����ұߵ�һ�����ŵ�λ��
		int pos1 = line.indexOf("[");// 0 ���û�� ����-1
		int pos2 = line.indexOf("]");
		if (pos1 == 0 && pos2 != -1) {
			// ����long���͵����� װ ʱ���
			long[] timePoints = new long[getTagCount(line)];
			// �õ�ʱ���
			String contentStr = line.substring(pos1 + 1, pos2);
			timePoints[0] = timeStrToLong(contentStr);
			if (timePoints[0] == -1) {
				return "";
			}
			String content = line;
			int i = 1;
			// �����whileѭ��������ʱ��ʱ������õ���
			while (pos1 == 0 && pos2 != -1) {
				content = content.substring(pos2 + 1);
				pos1 = content.indexOf("[");// 0 ���û�� ����-1
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
					// ����ڴ���
					lyric.setContent(content);
					// ʱ���
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
	 * ��ʱ���ת���ɺ���
	 * 
	 * @param content
	 * @return
	 */
	private long timeStrToLong(String content) {
		long result = 0;
		try {
			// ����content��02:04.12
			// �и��02��04.12
			// 04.12�и��04 12
			String[] s1 = content.split(":");
			String[] s2 = s1[1].split("\\.");

			// ��
			long min = Long.valueOf(s1[0]);
			// ��
			long second = Long.valueOf(s2[0]);
			// ����
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
	 * �ж��ж��پ��ʣ�����Ҫ����1
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
