package com.liqing.musicService;

interface MusicServiceAIDL{
	void playMusic(int id);//播放
	void nextMusic();//下一曲
	void lastMusic();//上一曲
	void stopMusic();//停止
	void pauseMusic();//暂停
	void setVolume(int leftVolume,int rightVolume);//设置声音
	int getDuration();//歌曲总时间
	int getCurrentTime();//当前播放时间
	void setCurrent(int cur);//快进，快退
	boolean isPlaying();//是否处于播放状态
	void releaseMediaPlayer();
	void setStart();
	void setEQ(int preset);
}