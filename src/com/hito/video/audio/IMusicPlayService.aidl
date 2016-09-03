package com.hito.video.audio;

import android.content.Intent;
import android.os.IBinder;

interface IMusicPlayService {

	/**
	 * ����λ��ȥ�� һ����Ƶ�ļ�������
	 * 
	 */
	 void notifyChanged (String action);
	

	/**
	* �ж������Ƿ��ڲ�����
	*/
	boolean isPlaying ();

	/**
	 * ����λ��ȥ�� һ����Ƶ�ļ�������
	 * 
	 * @param positon
	 */
	 void openAudio(int positon);

	/**
	 * ����
	 */
	  void play();

	/**
	 * ��ͣ
	 */
	  void pause();

	/**
	 * �õ���������Ϣ
	 * 
	 * @return
	 */
	  String getArtist() ;

	/**
	 * �õ��ļ�����
	 * 
	 * @return
	 */
	  String getName() ;

	/**
	 * �õ���������ʱ��
	 * 
	 * @return
	 */
	  int getDuration();

	/**
	 * �õ���ǰ����λ��
	 * 
	 * @return
	 */
	  int getCurrentPosition();

	/**
	 * ��λ����Ƶ�Ĳ���λ��
	 * 
	 * @param position
	 */
	  void seekTo(int position);

	/**
	 * ���ø����Ĳ���ģʽ��˳�򣬵�����ȫ��
	 * 
	 * @param model
	 */
	  void setPlayModel(int model) ;
	  /**
	  	*�õ�����ģʽ
	  */
	  int getPlayModel() ;

	/**
	 * ��һ��
	 */
	  void playPre() ;

	/**
	 * ��һ��
	 */
	  void playNext() ;
}
