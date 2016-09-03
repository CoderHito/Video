package com.hito.video.audio;

import android.content.Intent;
import android.os.IBinder;

interface IMusicPlayService {

	/**
	 * 根据位置去打开 一个音频文件并播放
	 * 
	 */
	 void notifyChanged (String action);
	

	/**
	* 判断音乐是否在播放中
	*/
	boolean isPlaying ();

	/**
	 * 根据位置去打开 一个音频文件并播放
	 * 
	 * @param positon
	 */
	 void openAudio(int positon);

	/**
	 * 播放
	 */
	  void play();

	/**
	 * 暂停
	 */
	  void pause();

	/**
	 * 得到艺术家信息
	 * 
	 * @return
	 */
	  String getArtist() ;

	/**
	 * 得到文件名称
	 * 
	 * @return
	 */
	  String getName() ;

	/**
	 * 得到歌曲的总时长
	 * 
	 * @return
	 */
	  int getDuration();

	/**
	 * 得到当前播放位置
	 * 
	 * @return
	 */
	  int getCurrentPosition();

	/**
	 * 定位到音频的播放位置
	 * 
	 * @param position
	 */
	  void seekTo(int position);

	/**
	 * 设置歌曲的播放模式：顺序，单曲，全部
	 * 
	 * @param model
	 */
	  void setPlayModel(int model) ;
	  /**
	  	*得到播放模式
	  */
	  int getPlayModel() ;

	/**
	 * 上一曲
	 */
	  void playPre() ;

	/**
	 * 下一曲
	 */
	  void playNext() ;
}
