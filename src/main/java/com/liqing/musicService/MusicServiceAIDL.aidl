package com.liqing.musicService;

interface MusicServiceAIDL{
	void playMusic(int id);//����
	void nextMusic();//��һ��
	void lastMusic();//��һ��
	void stopMusic();//ֹͣ
	void pauseMusic();//��ͣ
	void setVolume(int leftVolume,int rightVolume);//��������
	int getDuration();//������ʱ��
	int getCurrentTime();//��ǰ����ʱ��
	void setCurrent(int cur);//��������ˡ���
	boolean isPlaying();//�Ƿ��ڲ���״̬
	void releaseMediaPlayer();
	void setStart();
	void setEQ(int preset);
}