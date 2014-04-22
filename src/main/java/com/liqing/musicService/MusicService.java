package com.liqing.musicService;

import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.animation.AnimationUtils;
import com.liqing.R;
import com.liqing.activities.MusicActivity;
import com.liqing.bean.Music;
import com.liqing.bean.MusicList;
import com.liqing.mediaplayer_music.MainActivity;
import com.liqing.util.*;
import com.liqing.util.LrcProcess.LrcContent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {

	private  MediaPlayer mediaPlayer = null;
	private final static int STOP = 0;
	private final static int PAUSE = 1;
	private final static int PLAYING = 2;
	private final static int START = 3;
	private int state = START;// ����״̬��־
	
	public static int currentIndex = 0;// ��ǰ���ŵĵط�
	public static String currentPath = null;
	public static final String LIST_NAME = "listname";
	public static final String ID = "id";
	public static String duration = null;
	public static String musicName = null;
	public static String singer = null;
	public static String albumArt = null;// ר��������

	public final static int Single = 1;
	public final static int Circle = 2;
	public final static int Random = 3;

	public static int State;// ����ģʽ

	private Random mRandom = null;

	private Cursor currentCursor = null;

	private MusicList musicList = null;

	private static LrcProcess lrcProcess = null;
	private int nowindex=0;

	private Equalizer equalizer;

	Handler mHandler = new Handler();

	// ��ʹ����߳�
	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			MusicActivity.lrcView.invalidate();
			if(nowindex!=LrclrcIndex()&&MusicActivity.drawchi)
			{
				MusicActivity.lrcView.SetIndex(LrclrcIndex());
				Mythread mt=new Mythread(gettime(LrclrcIndex()));
			mt.start();
			nowindex=LrclrcIndex();
			}
			if(!MusicActivity.down)
			{	MusicActivity.currentTime.setText(Music.toTime(mediaPlayer
					.getCurrentPosition()));
			MusicActivity.currentTime.invalidate();
			MusicActivity.progress.setProgress((int) (mediaPlayer
					.getCurrentPosition() * 100 / Integer.valueOf(duration)));
			}
			mHandler.postDelayed(mRunnable, 1000);
		}
	};
    public class Mythread extends Thread
    {
    	int frame=50;
    	int time;
    	public Mythread (int time)
    	{
    		this.time=time;
    	}
    	@Override
		public void run()
    	{
    		for(int i=0;i<frame;i++)
    		{
    			if(!MusicActivity.isdraw)
    			{
    				MusicActivity.isdraw=true;
    				MusicActivity.lrcView.setframe(0);
    				break;
    			}
    			MusicActivity.lrcView.setframe(i);
    			MusicActivity.lrcView.postInvalidate();
    			
    			try {
					Thread.sleep(time/frame);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			
    		}
    	}
    }
    
	public  int gettime(int index)
	{
		int  time=0;
		if(index+1<lrcList.size())
		time=lrcList.get(index+1).getLrc_time()-lrcList.get(index).getLrc_time();
		return time/2 ;
		
	}
	
	/**
	 * ���ͬ��������
	 */
	public int LrclrcIndex() {

		if (mediaPlayer.isPlaying()) {
			// ��ø�������ĵ�ʱ��
			CurrentTime = mediaPlayer.getCurrentPosition();
			// ��ø�����ʱ�䳤��
			CountTime = mediaPlayer.getDuration();
		}

		if (CurrentTime < CountTime) {

			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (CurrentTime < lrcList.get(i).getLrc_time() && i == 0) {
						lrcIndex = i;
					}
					if (CurrentTime > lrcList.get(i).getLrc_time()
							&& CurrentTime < lrcList.get(i + 1).getLrc_time()) {
						lrcIndex = i;
					}
				}
				if (i == lrcList.size() - 1
						&& CurrentTime > lrcList.get(i).getLrc_time()) {
					lrcIndex = i;
				}
			}
		}
		return lrcIndex;
	}

	// ��������
	private List<LrcContent> lrcList = new ArrayList<LrcContent>();

	// ��ʼ����ʼ���ֵ
	public static int lrcIndex = 0;
	// ��ʼ�������ʱ��ı���
	public static int CurrentTime = 0;
	// ��ʼ��������ʱ��ı���
	public static int CountTime = 0;

	private final MusicServiceAIDL.Stub musicServiceBinder = new MusicServiceAIDL.Stub() {

		@Override
		public void playMusic(int id) throws RemoteException {
			MusicService.this.playMusic(id);
		}

		@Override
		public void nextMusic() throws RemoteException {
			MusicService.this.nextMusic();
		}

		@Override
		public void lastMusic() throws RemoteException {
			MusicService.this.lastMusic();
		}

		@Override
		public void stopMusic() throws RemoteException {
			MusicService.this.stopMusic();
		}

		@Override
		public void pauseMusic() throws RemoteException {
			MusicService.this.pauseMusic();
		}

		@Override
		public void setVolume(int leftVolume, int rightVolume)
				throws RemoteException {
			MusicService.this.setVolume(leftVolume, rightVolume);
		}

		@Override
		public int getDuration() throws RemoteException {
			return MusicService.this.getDuration();
		}

		@Override
		public int getCurrentTime() throws RemoteException {
			return MusicService.this.getCurrentTime();
		}

		@Override
		public void setCurrent(int cur) throws RemoteException {
			MusicService.this.setCurrent(cur);
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			return MusicService.this.isPlaying();
		}

		@Override
		public void releaseMediaPlayer() throws RemoteException {
			MusicService.this.releaseMediaPlayer();
		}

		@Override
		public void setStart() throws RemoteException {
			MusicService.this.setStart();
		}

		@Override
		public void setEQ(int eq) throws RemoteException {
			MusicService.this.setEQ(eq);
		}
	};

	// �����ⲿ������
	private BroadcastReceiver playKeyDownReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			KeyEvent keyEvent = intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);// �ⲿ�����¼�

			// ���Ż���ͣ
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				// ���ڲ��ţ�����ͣ��
				if (isPlaying()) {
					pauseMusic();
				} else {
					playMusic(currentIndex);
				}
			}
			// ��һ��
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
				nextMusic();
			}
			// ��һ��
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
				lastMusic();
			}
			// ֹͣ
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_STOP) {
				stopMusic();
			}

			// ���
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
				// �����
			}

			// ����
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_REWIND) {
				// �����
			}
		}
	};

	// ��ò��Ÿ����Cursor��position

	@Override
	public IBinder onBind(Intent intent) {
		return musicServiceBinder;
	}

	private void setEQ(int preset) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PreferenceKeys.CURRENTEQ, preset);
		editor.commit();
		equalizer.usePreset((short) preset);
	}

	@Override
	public void onCreate() {
		mediaPlayer = MediaPlayer.create(MusicService.this, R.raw.ring);// ����������
		mediaPlayer.start();
		super.onCreate();

		equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
		equalizer.setEnabled(true);
		
		MusicService.lrcProcess = new LrcProcess();

		// ��̬���ⲿý�岥����ͣ�Ȱ��������
		this.registerReceiver(playKeyDownReceiver, new IntentFilter(
				Intent.ACTION_MEDIA_BUTTON));

		this.musicList = new MusicList(getApplicationContext());

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		currentIndex = sharedPreferences
				.getInt(PreferenceKeys.CURRENT_INDEX, 0);
		currentPath = sharedPreferences.getString(PreferenceKeys.CURRENT_PATH,
				"");
		duration = sharedPreferences.getString(PreferenceKeys.DURATION, "0");
		musicName = sharedPreferences
				.getString(PreferenceKeys.MUSIC_NAME, "����");
		singer = sharedPreferences.getString(PreferenceKeys.SINGER, "����");
		State = sharedPreferences.getInt(PreferenceKeys.MODE, Circle);
		albumArt = sharedPreferences.getString(PreferenceKeys.ALBUMART, null);
		lrcProcess.readLRC(currentPath);
		lrcList = lrcProcess.getLrcContent();
		
		setEQ(sharedPreferences.getInt(PreferenceKeys.CURRENTEQ, 0));

		MusicActivity.lrcView.setSentenceEntities(lrcList);
		MusicActivity.musicView.refreshBitmap(albumArt);
		currentCursor = musicList.getListMusic(MainActivity.LIST_name);

		this.mRandom = new Random();

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// Cursor cursor =
				// musicList.getListMusic(MusicList.DEFAULT_TABLE_NAME);

				// ���¸���Ĭ���б�
				updateDefaultList(FileUtil.SDCardRoot + FileUtil.MUSICPATH);

				// // ************����ר������ 150*150��ͼ**************
				// int index = currentIndex;
				//
				// Cursor tempCursor = musicList
				// .getListMusic(MainActivity.LIST_name);
				//
				// NetUtil netUtil = new NetUtil(getApplicationContext());
				// netUtil.setListName(MainActivity.LIST_name);
				//
				// for (int i = index; i < tempCursor.getCount(); i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				//
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				// String target = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ALBUM_ART));
				// if (target == null || target.length() < 1
				// || !FileUtil.isFileExist(target + ".png",
				// FileUtil.ALBUMPATH)) {
				// netUtil.setMessage(tempmusicname, tempsinger, null);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.downloadAlbumArt();
				// }
				// }
				// }
				//
				// for (int i = 0; i < index; i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				//
				// String target = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ALBUM_ART));
				// if (target == null || target.length() < 1
				// || !FileUtil.isFileExist(target + ".png",
				// FileUtil.ALBUMPATH)) {
				// netUtil.setMessage(tempmusicname, tempsinger, null);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.downloadAlbumArt();
				// }
				// }
				// }
				//
				// // ************���ظ��**************
				// for (int i = index; i < tempCursor.getCount(); i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				//
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				// String temppath = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.PATH));
				// if (temppath != null && temppath.length() > 0
				// && !FileUtil._isFileExist(temppath.replace(".mp3", ".lrc")))
				// {
				// netUtil.setMessage(tempmusicname, tempsinger, temppath);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.downloadLrc();
				// }
				// }
				// }
				//
				// for (int i = 0; i < index; i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				//
				// String temppath = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.PATH));
				// if (temppath != null && temppath.length() > 0
				// && !FileUtil._isFileExist(temppath.replace(".mp3", ".lrc")))
				// {
				// netUtil.setMessage(tempmusicname, tempsinger, temppath);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.downloadLrc();
				// }
				// }
				// }

				// ************���ظ���ͷ��**************
				// for (int i = index; i < tempCursor.getCount(); i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				//
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				// String target = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ALBUM_ART));
				// if (target == null
				// || !FileUtil.isFileExist(target + ".png",
				// FileUtil.ALBUMPATH)) {
				// netUtil.setMessage(tempmusicname, tempsinger, null);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.getAlbumArt();
				// }
				// }
				// }
				//
				// for (int i = 0; i < index; i++) {
				// tempCursor.moveToPosition(i);
				// String tempmusicname = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.TITLE_KEY));
				// String tempsinger = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ARTIST));
				//
				// String target = tempCursor.getString(tempCursor
				// .getColumnIndex(Music.ALBUM_ART));
				// if (target == null
				// || !FileUtil.isFileExist(target + ".png",
				// FileUtil.ALBUMPATH)) {
				// netUtil.setMessage(tempmusicname, tempsinger, null);
				// boolean is = NetUtil
				// .isNetConnection(getApplicationContext());
				// if (is) {
				// netUtil.getAlbumArt();
				// }
				// }
				// }
			}
		});
		thread.start();
	}

	private void updateDefaultList(String path) {
		if(!FileUtil._isFilePathExist(path)){
			return;
		}
		File file = new File(path);
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				updateDefaultList(files[i].getAbsolutePath());
			} else if (files[i].getName().contains(".mp3")) {
				// ���浽��ݿ�
				Music music = MusicList.getMusic(getApplicationContext(),
						files[i].getAbsolutePath());
				NetUtil netUtil = new NetUtil(getApplicationContext());
				netUtil.setListName(MusicList.DEFAULT_TABLE_NAME);
				netUtil.setMessage(music.getMusicName(), music.getArtist(),
						music.getPath());
				if (!musicList.hasExistListName(MusicList.DEFAULT_TABLE_NAME,
						music.getMusicName())) {
					musicList.saveToList(music, MusicList.DEFAULT_TABLE_NAME);
					netUtil.downloadAlbumArt();
				}
				// ����ר������͸��
				netUtil.downloadLrc();
			}
		}
	}

	@Override
	public void onDestroy() {
		// ȡ��̬��
		this.unregisterReceiver(playKeyDownReceiver);
		musicList.closeDatabase();
	}

	/**
	 * getPath
	 * 
	 * @param id
	 *            ���ֵ������
	 * @return path ��Ӧ����ŵ����ֵ�·��
	 */
	private String getPath(int id) {

		currentCursor = musicList.getListMusic(MainActivity.LIST_name);
		currentCursor.moveToPosition(id);
		currentPath = currentCursor.getString(currentCursor
				.getColumnIndex(Music.PATH));

		lrcProcess.readLRC(currentPath);
		lrcList = lrcProcess.getLrcContent();
		
		MusicActivity.lrcView.setSentenceEntities(lrcList);

		duration = currentCursor.getString(currentCursor
				.getColumnIndex(Music.DURATION));

		MusicActivity.currentTime.setText("00:00");
		musicName = currentCursor.getString(currentCursor
				.getColumnIndex(Music.TITLE_KEY));
		singer = currentCursor.getString(currentCursor
				.getColumnIndex(Music.ARTIST));
		MusicActivity.endTime.setText(Music.toTime(Integer.valueOf(duration)));
		MusicActivity.musicName.setText(musicName);
		MusicActivity.singer.setText(singer);

		if (MainActivity.isBackground(getApplicationContext())) {
			MyNotification notification = new MyNotification(
					getApplicationContext());
			notification.showNotification(musicName, singer);
		}

		albumArt = currentCursor.getString(currentCursor
				.getColumnIndex(Music.ALBUM_ART));
		MusicActivity.musicView.refreshBitmap(albumArt);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PreferenceKeys.CURRENT_INDEX, MusicService.currentIndex);
		editor.putString(PreferenceKeys.MUSIC_NAME, MusicService.musicName);
		editor.putString(PreferenceKeys.SINGER, singer);
		editor.putString(PreferenceKeys.DURATION, MusicService.duration);
		editor.putString(PreferenceKeys.CURRENT_PATH, MusicService.currentPath);
		editor.putString(PreferenceKeys.ALBUMART, albumArt);
		editor.commit();

		return currentPath;
	}

	public static boolean isNoLrc(){
		return lrcProcess.getLrcContent().isEmpty();
	}
	
	private void playMusic(int id) {
		if (currentIndex == id && state == PLAYING
				&& currentCursor.getCount() != 1) {
			return;
		}
		if (currentCursor.getCount() == 0) {
			currentCursor = musicList.getListMusic(MainActivity.LIST_name);
			if (currentCursor.getCount() == 0)
				return;
		}

		currentIndex = id;
		String path = getPath(id);

		if (state == STOP || state == PLAYING || state == START) {
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();
				mediaPlayer.seekTo(MusicActivity.progress.getProgress()
						* Integer.valueOf(duration) / 100);// ����ǰ�����˽�ȵ�ʱ�������
				if(state == PLAYING || state == START){
					mediaPlayer.start();
					// �л�����ʾ���
						MusicActivity.lrcView.setAnimation(AnimationUtils
							.loadAnimation(MusicService.this, R.anim.alpha_z));
					 //�����߳�
					mHandler.post(mRunnable);
					state = PLAYING;
				}
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (state == PAUSE) {
			mediaPlayer.start();
			state = PLAYING;
		}

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				nextMusic();
			}
		});
		saveToRecent();
	}

	private void nextMusic() {
		MusicActivity.progress.setProgress(0);

		if (currentCursor.getCount() == 0) {
			currentCursor = musicList.getListMusic(MainActivity.LIST_name);
			if (currentCursor.getCount() == 0)
				return;
		}
		int id = 0;
		if (State == Circle) {
			id = currentIndex;
			if (++id == currentCursor.getCount()) {
				id = 0;
			}
		} else if (State == Single) {
			id = currentIndex;
		} else if (State == Random) {
			id = mRandom.nextInt(currentCursor.getCount());
		}
		if(state == PAUSE || state == STOP || state == START){
			state = STOP;
		}
		playMusic(id);
	}

	private void lastMusic() {
		MusicActivity.progress.setProgress(0);
		if (currentCursor.getCount() == 0) {
			currentCursor = musicList.getListMusic(MainActivity.LIST_name);
			if (currentCursor.getCount() == 0)
				return;
		}
		int id = 0;
		if (State == Circle) {
			id = currentIndex;
			if (--id < 0) {
				id = currentCursor.getCount() - 1;
			}
		} else if (State == Single) {
			id = currentIndex;
		} else if (State == Random) {
			id = mRandom.nextInt(currentCursor.getCount() - 1);
		}
		if(state == PAUSE || state == STOP || state == START){
			state = STOP;
		}
		playMusic(id);
	}

	private void setStart(){
		this.state = START;
	}

	private void saveToRecent() {
		if (!MainActivity.LIST_name.equals("recent")) {
			currentCursor.moveToPosition(currentIndex);

			Music music = new Music(currentCursor.getString(currentCursor
					.getColumnIndex(Music.TITLE_KEY)),
					currentCursor.getString(currentCursor
							.getColumnIndex(Music.DURATION)),
					currentCursor.getString(currentCursor
							.getColumnIndex(Music.ARTIST)),
					currentCursor.getString(currentCursor
							.getColumnIndex(Music.ALBUM)),
					currentCursor.getString(currentCursor
							.getColumnIndex(Music.PATH)),
					currentCursor.getString(currentCursor
							.getColumnIndex(Music.ALBUM_ART)));

			musicList.saveToList(music, "recent");
		}
	}

	private void stopMusic() {
		if (state != STOP) {
			mediaPlayer.stop();
			state = STOP;
		}
	}

	private void pauseMusic() {
		if (state == PLAYING) {
			mediaPlayer.pause();
			state = PAUSE;
		}
	}

	private void setVolume(int leftVolume, int rightVolume) {
		mediaPlayer.setVolume(leftVolume, rightVolume);
	}

	private int getDuration() {
		return mediaPlayer.getDuration();
	}

	private int getCurrentTime() {
		return mediaPlayer.getCurrentPosition();
	}

	private void setCurrent(int cur) {
		MusicActivity.progress.setProgress(cur * 100 / Integer.valueOf(duration));
		mediaPlayer.seekTo(cur);
	}

	private boolean isPlaying() {
		if (state == PLAYING) {
			return true;
		} else {
			return false;
		}
	}

	private void releaseMediaPlayer() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
}
