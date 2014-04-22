package com.liqing.mediaplayer_music;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.liqing.R;
import com.liqing.activities.*;
import com.liqing.bean.MusicList;
import com.liqing.musicService.MusicService;
import com.liqing.musicService.MusicServiceAIDL;
import com.liqing.util.MyNotification;
import com.liqing.util.PreferenceKeys;
import com.liqing.views.VolumeView;

import java.util.ArrayList;
import java.util.List;

//ʹ��Service���񲥷�
public class MainActivity extends TabActivity implements OnClickListener, OnCheckedChangeListener
{

	/* ����������ť */
	private ImageButton mode, last, play_pause, next, volume;
	private VolumeView volumeView = null;// ����
	private FrameLayout frameVolume;

	public static String userName;

	/* ��ǰ���Ÿ�������� */
	public int currentIndex = 0;

	public static String LIST_name = null;

	private TabHost tabHost = null;
	private RadioButton music_tab = null, musiclist_tab = null, recent_tab = null, lastRadioButton;
	private Intent musicIntent, musicListIntent, recentIntent;// , artistIntent;

	public static String SEEKBAR_ACTION = "com.liqing.action.seekbarchange";
	public static final String SELECTED_ACTION = "com.liqing.action.playselected";

	public static AudioManager audioManager = null;
	public static int Volume_Max = 0;

	private ViewPager viewPager = null;
	private MyPagerAdapter myPagerAdapter = null;
	private ArrayList<View> list = null;
	private LocalActivityManager manager = null;

	private MusicServiceAIDL appService = null;

	public static boolean isPushThisToStack = false;

	private ServiceConnection onserver = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			appService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			appService = MusicServiceAIDL.Stub.asInterface(service);// �ڱ��ش�����һ������
		}
	};

	private BroadcastReceiver selectedBroadcastReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals(SELECTED_ACTION))
			{
				String listName = intent.getStringExtra(MusicService.LIST_NAME);
				int position = intent.getIntExtra(MusicService.ID, 0);
				MusicActivity.progress.setProgress(0);
				// ����б��position��������ʾ���
				currentIndex = MusicService.currentIndex;
				if (listName.equals(LIST_name) && MusicService.currentIndex == position)
				{
					return;
				}
				LIST_name = listName;
				currentIndex = position;

				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.MUSIC_LIST, LIST_name);
				editor.putInt(PreferenceKeys.CURRENT_INDEX, currentIndex);
				editor.commit();

				((RadioButton) findViewById(R.id.radio_button0)).setChecked(true);
				try
				{
					appService.setStart();
					playMusic();
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
		}
	};

	private BroadcastReceiver seekChangedReceiver = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals(SEEKBAR_ACTION))
			{
				int current = intent.getIntExtra("current", 0);
				setCurrent(current);
			}
		}
	};

	private boolean isPhone = false, isplay = false;

	// �绰״̬����
	private class MobliePhoneStateListener extends PhoneStateListener
	{

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
			case TelephonyManager.CALL_STATE_IDLE: /* �绰���� */
				if (isPhone)
				{
					try
					{
						if (isplay)
						{
							appService.playMusic(MusicService.currentIndex);
							isplay = false;
						}
					} catch (RemoteException e)
					{
						e.printStackTrace();
					}
					isPhone = false;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: /* ����绰ʱ */

			case TelephonyManager.CALL_STATE_RINGING: /* �绰����ʱ */
				isplay = isPlaying();
				try
				{
					appService.pauseMusic();
					if (!isPhone)
						isPhone = true;
				} catch (RemoteException e)
				{
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		Intent serviceIntent = new Intent("com.liqing.action.musicservice");
		this.bindService(serviceIntent, onserver, Context.BIND_AUTO_CREATE);

		this.registerReceiver(selectedBroadcastReceiver, new IntentFilter(SELECTED_ACTION));
		this.registerReceiver(seekChangedReceiver, new IntentFilter(SEEKBAR_ACTION));

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Volume_Max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		this.tabHost = this.getTabHost();

		this.musicIntent = new Intent(MainActivity.this, MusicActivity.class);
		this.musicListIntent = new Intent(MainActivity.this, MusicListActivity.class);
		this.recentIntent = new Intent(MainActivity.this, RecentActivity.class);
		// this.artistIntent = new Intent(MainActivity.this, ArtistActivity.class);

		this.tabHost.addTab(buildTabSpec("music_tab", R.string.music_tab, musicIntent));
		this.tabHost.addTab(buildTabSpec("musiclist_tab", R.string.musiclist_tab, musicListIntent));
		this.tabHost.addTab(buildTabSpec("recent_tab", R.string.recent_tab, recentIntent));
		// this.tabHost.addTab(buildTabSpec("artist_tab", R.string.artist_tab,
		// artistIntent));

		music_tab = (RadioButton) this.findViewById(R.id.radio_button0);
		lastRadioButton = music_tab;

		musiclist_tab = (RadioButton) this.findViewById(R.id.radio_button1);
		recent_tab = (RadioButton) this.findViewById(R.id.radio_button2);
		// RadioButton artist_tab = (RadioButton) this
		// .findViewById(R.id.radio_button3);
		music_tab.setOnCheckedChangeListener(this);
		musiclist_tab.setOnCheckedChangeListener(this);
		recent_tab.setOnCheckedChangeListener(this);
		// artist_tab.setOnCheckedChangeListener(this);

		this.tabHost.setCurrentTab(0);

		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		LIST_name = sharedPreferences.getString(PreferenceKeys.MUSIC_LIST, MusicList.DEFAULT_TABLE_NAME);
		MusicService.State = sharedPreferences.getInt(PreferenceKeys.MODE, MusicService.Circle);

		initView();
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new MobliePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * �ӵ���onStart()������onStop()֮����activity�Ŀɼ��������ڡ� �����ʱ�ڣ���Ȼ����ܲ�����ǰ�棬���û���������Ļ�Ͽ����activity��
	 * ������������֮�䣬�����ά����Ҫ����Դ����activity��ʾ���û���
	 */
	@Override
	public void onStart()
	{
		isPushThisToStack = false;
		super.onStart();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (!isPushThisToStack)
		{
			this.moveToBack();
		}
	}

	private void moveToBack()
	{
		MyNotification myNotification = new MyNotification(getApplicationContext());
		myNotification.showNotification(MusicService.musicName, MusicService.singer);
		moveTaskToBack(true);// ��activityѹջ
	}

	/**
	 * �ӵ���onResume()������onPause()��activity��ǰ��������, �����ʱ�ڣ�activity����������activity��ǰ�沢�ҿ����û����н�����
	 * һ��activity���������»ָ�����ͣ״̬֮��Ƶ��ת���� ���磬���豸���߻���һ���µ�activity����ʱonPause()�����ᱻ���ã� ��һ��activity
	 * result����һ���µ�intent���󱻽��յ�ʱonResume()�ᱻ���á� ��ˣ��������������еĴ���Ӧ�����������ġ�
	 */
	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	/**
	 * activityͻȻ������activityռ�ݣ�������activity�رպ��ڸǵ�activity����
	 */
	@Override
	public void onRestart()
	{
		super.onRestart();
	}

	private TabHost.TabSpec buildTabSpec(String tag, int resLable, final Intent intent)
	{
		return this.tabHost.newTabSpec(tag).setIndicator(getString(resLable), null).setContent(intent);
	}

	private View getView(String id, Intent intent)
	{
		View view = manager.startActivity(id, intent).getDecorView();// ��������������activity��View
		return view;
	}

	private void initView()
	{
		this.viewPager = (ViewPager) this.findViewById(R.id.viewpager);
		list = new ArrayList<View>();
		this.myPagerAdapter = new MyPagerAdapter(list);
		list.add(getView("musicactivity", new Intent(this, MusicActivity.class)));
		list.add(getView("musiclistactivity", new Intent(this, MusicListActivity.class)));
		list.add(getView("recentactivity", new Intent(this, RecentActivity.class)));
		// list.add(getView("artistactivity", new Intent(this,
		// ArtistActivity.class)));
		this.viewPager.setAdapter(myPagerAdapter);
		this.viewPager.setCurrentItem(0);
		this.viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

		this.mode = (ImageButton) this.findViewById(R.id.mode);
		this.last = (ImageButton) this.findViewById(R.id.last);
		this.play_pause = (ImageButton) this.findViewById(R.id.play_pause);
		this.next = (ImageButton) this.findViewById(R.id.next);
		this.volume = (ImageButton) this.findViewById(R.id.volume);

		this.mode.setOnClickListener(this);
		this.last.setOnClickListener(this);
		this.play_pause.setOnClickListener(this);
		this.next.setOnClickListener(this);
		this.volume.setOnClickListener(this);

		this.frameVolume = (FrameLayout) this.findViewById(R.id.frameVolume);
		this.volumeView = (VolumeView) this.findViewById(R.id.volumeview);

		if (MusicService.State == MusicService.Circle)
		{
			this.mode.setImageResource(R.drawable.state_circle);
		} else if (MusicService.State == MusicService.Random)
		{
			this.mode.setImageResource(R.drawable.state_random);
		} else if (MusicService.State == MusicService.Single)
		{
			this.mode.setImageResource(R.drawable.state_single);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		this.unregisterReceiver(selectedBroadcastReceiver);
		this.unregisterReceiver(seekChangedReceiver);
		this.unbindService(onserver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.activity_music, menu);
		menu.add(Menu.NONE, 1, 1, "�˳�");
		menu.add(Menu.NONE, 2, 1, "��Ч");
		menu.add(Menu.NONE, 3, 1, "�˻�");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
		case 1:
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(PreferenceKeys.MUSIC_LIST, LIST_name);
			editor.putInt(PreferenceKeys.CURRENT_INDEX, MusicService.currentIndex);
			editor.putString(PreferenceKeys.SINGER, MusicService.singer);
			editor.putString(PreferenceKeys.MUSIC_NAME, MusicService.musicName);
			editor.putString(PreferenceKeys.DURATION, MusicService.duration);
			editor.putString(PreferenceKeys.CURRENT_PATH, MusicService.currentPath);
			editor.putInt(PreferenceKeys.MODE, MusicService.State);
			editor.putString(PreferenceKeys.ALBUMART, MusicService.albumArt);
			editor.commit();

			try
			{
				appService.releaseMediaPlayer();
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}

			this.unbindService(onserver);
			this.unregisterReceiver(selectedBroadcastReceiver);
			this.unregisterReceiver(seekChangedReceiver);
			System.exit(0);
			break;
		case 2:
			Intent intent = new Intent(this, EqualizerActivity.class);
			SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			int preset = sharedPreferences1.getInt(PreferenceKeys.CURRENTEQ, 0);
			intent.putExtra("currentPreset", preset);
			isPushThisToStack = true;
			this.startActivityForResult(intent, 0);
			break;
		case 3:
			// TODO: goto the setting activity
			Intent intent1 = new Intent(this, AccountActivity.class);
			isPushThisToStack = true;
			this.startActivity(intent1);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			try
			{
				Log.d("preset in result", String.valueOf(data.getIntExtra("preset", 0)));
				appService.setEQ(data.getIntExtra("preset", 0));
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean isPlaying()
	{
		try
		{
			return appService.isPlaying();
		} catch (RemoteException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isBackground(Context context)
	{
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses)
		{
			if (appProcess.processName.equals(context.getPackageName()))
			{
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
				{
					return true;
				} else
				{
					return false;
				}
			}
		}
		return false;
	}

	// ���ֲ��ſ��ư�ť������
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.mode:
			// �л�ģʽ
			if (MusicService.State == MusicService.Circle)
			{
				// ��Ϊ���
				MusicService.State = MusicService.Random;
				mode.setImageResource(R.drawable.state_random);
			} else if (MusicService.State == MusicService.Random)
			{
				// ��Ϊ����
				MusicService.State = MusicService.Single;
				mode.setImageResource(R.drawable.state_single);
			} else if (MusicService.State == MusicService.Single)
			{
				// ��Ϊѭ��
				MusicService.State = MusicService.Circle;
				mode.setImageResource(R.drawable.state_circle);
			}
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt(PreferenceKeys.MODE, MusicService.State);
			editor.commit();
			break;
		case R.id.last:
			SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			currentIndex = sharedPreferences1.getInt(PreferenceKeys.CURRENT_INDEX, 0);
			LIST_name = sharedPreferences1.getString(PreferenceKeys.MUSIC_LIST, MusicList.DEFAULT_TABLE_NAME);
			frontPlay();
			if (isPlaying())
			{
				play_pause.setImageResource(R.drawable.controlbar_pause);
			} else
			{
				play_pause.setImageResource(R.drawable.controlbar_play);
			}
			break;
		case R.id.play_pause:
			SharedPreferences sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			currentIndex = sharedPreferences2.getInt(PreferenceKeys.CURRENT_INDEX, 0);
			LIST_name = sharedPreferences2.getString(PreferenceKeys.MUSIC_LIST, MusicList.DEFAULT_TABLE_NAME);
			try
			{
				if (isPlaying())
				{
					play_pause.setImageResource(R.drawable.controlbar_play);
					appService.pauseMusic();
				} else
				{
					appService.setStart();
					play_pause.setImageResource(R.drawable.controlbar_pause);
					playMusic();
				}
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
			break;
		case R.id.next:
			SharedPreferences sharedPreferences3 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			currentIndex = sharedPreferences3.getInt(PreferenceKeys.CURRENT_INDEX, 0);
			LIST_name = sharedPreferences3.getString(PreferenceKeys.MUSIC_LIST, MusicList.DEFAULT_TABLE_NAME);
			nextPlay();
			if (isPlaying())
			{
				play_pause.setImageResource(R.drawable.controlbar_pause);
			} else
			{
				play_pause.setImageResource(R.drawable.controlbar_play);
			}
			break;
		case R.id.volume:
			// ��������
			if (this.volumeView.getVisibility() == View.GONE)
			{
				this.volumeView.postInvalidate();
				this.volumeView.setVisibility(View.VISIBLE);
				this.volumeView.setFocusable(true);
			} else
			{
				this.volumeView.setVisibility(View.GONE);
				this.volumeView.setFocusable(false);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent keyEvent)
	{
		if (keycode == KeyEvent.KEYCODE_BACK)
		{
			this.moveToBack();
			return true;
		}
		return super.onKeyDown(keycode, keyEvent);
	}

	// ����ŵ�ǰ����
	private void playMusic() throws RemoteException
	{
		appService.playMusic(currentIndex);
		if (isPlaying())
		{
			play_pause.setImageResource(R.drawable.controlbar_pause);
		}
	}

	private void setCurrent(int current)
	{
		try
		{
			appService.setCurrent(current);
		} catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}

	private void nextPlay()
	{
		try
		{
			appService.nextMusic();
		} catch (RemoteException e)
		{
			e.printStackTrace();
		}
		if (isPlaying())
		{
			this.play_pause.setImageResource(R.drawable.controlbar_pause);
		} else
		{
			this.play_pause.setImageResource(R.drawable.controlbar_play);
		}
	}

	/* ��һ�� */
	private void frontPlay()
	{
		try
		{
			appService.lastMusic();
		} catch (RemoteException e)
		{
			e.printStackTrace();
		}
		if (isPlaying())
		{
			this.play_pause.setImageResource(R.drawable.controlbar_pause);
		} else
		{
			this.play_pause.setImageResource(R.drawable.controlbar_play);
		}
	}

	// ��Ϊ��TabHost�Ժ�onKeyDown����ʵ���Ǳ���Activity��ȡ��ͨ��dispatchKeyEvent����������back��
	// @Override
	// public boolean dispatchKeyEvent(KeyEvent keyEvent) {
	// if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	// try {
	// SharedPreferences sharedPreferences = PreferenceManager
	// .getDefaultSharedPreferences(getApplicationContext());
	// SharedPreferences.Editor editor = sharedPreferences.edit();
	// editor.putString("MusicList", LIST_name);
	// editor.putInt("currentIndex", MusicService.currentIndex);
	// editor.putString("singer", MusicService.singer);
	// editor.putString("musicname", MusicService.musicName);
	// editor.putString("duration", MusicService.duration);//
	// editor.putInt("lrcIndex", MusicService.lrcIndex);
	// editor.putString("currentPath", MusicService.currentPath);
	// editor.commit();
	//
	// appService.releaseMediaPlayer();
	// this.unbindService(onserver);
	// } catch (RemoteException e) {
	// e.printStackTrace();
	// }
	// System.exit(0);
	// // ״̬����פ
	// }
	// return super.dispatchKeyEvent(keyEvent);
	// }

	// RadioGroup��ѡ�񰴼���
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			int position = buttonView.getId();
			if (lastRadioButton != null)
			{
				this.lastRadioButton.setEnabled(true);
			}
			switch (position)
			{
			case R.id.radio_button0:

				this.music_tab.setEnabled(false);
				lastRadioButton = this.music_tab;

				this.tabHost.setCurrentTabByTag("music_tab");

				// �����һ�ν������ViewPager��view
				// manager.dispatchResume();
				manager.getActivity("musicactivity").onContentChanged();
				list.remove(0);
				list.add(0, getView("musicactivity", new Intent(this, MusicActivity.class)));

				viewPager.setCurrentItem(0);
				break;
			case R.id.radio_button1:
				this.musiclist_tab.setEnabled(false);
				lastRadioButton = this.musiclist_tab;

				this.tabHost.setCurrentTabByTag("musiclist_tab");

				// manager.getActivity("musiclistactivity").onContentChanged();
				// list.remove(1);
				// list.add(
				// 1,
				// getView("musiclistactivity", new Intent(this,
				// MusicListActivity.class)));

				viewPager.setCurrentItem(1);
				break;
			case R.id.radio_button2:
				this.recent_tab.setEnabled(false);
				lastRadioButton = this.recent_tab;

				this.tabHost.setCurrentTabByTag("recent_tab");

				manager.getActivity("recentactivity").onContentChanged();
				list.remove(2);
				list.add(2, getView("recentactivity", new Intent(this, RecentActivity.class)));

				viewPager.setCurrentItem(2);
				break;
			// case R.id.radio_button3:
			// this.tabHost.setCurrentTabByTag("artist_tab");
			//
			// manager.getActivity("artistactivity").onContentChanged();
			// list.remove(3);
			// list.add(
			// 3,
			// getView("artistactivity", new Intent(this,
			// ArtistActivity.class)));
			//
			// viewPager.setCurrentItem(3);
			// break;
			default:
				break;
			}
			// activity�л�ʱ����,ȡ������Ϊ������ViewPager�Ķ����ظ�
			// this.tabHost.getTabContentView().setAnimation(AnimationUtils.loadAnimation(this,enterAnim));
			// this.overridePendingTransition(enterAnim,
			// exitAnim);//����startActivity����������Ч
		}
	}

	private class MyPagerAdapter extends PagerAdapter
	{

		ArrayList<View> views = null;

		public MyPagerAdapter(ArrayList<View> views)
		{
			this.views = views;
		}

		@Override
		public int getCount()
		{
			if (views != null)
				return views.size();
			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2)
		{
			((ViewPager) arg0).removeView(views.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0)
		{
		}

		@Override
		public Object instantiateItem(View arg0, int position)
		{
			((ViewPager) arg0).addView(views.get(position), 0);
			// �����¸�����
			int last = (position - 1);
			if (last == -1)
				last = 3;
			int next = (position + 1) % views.size();

			switch (last)
			{
			case 0:
				views.remove(last);
				manager.getActivity("musicactivity").onContentChanged();
				views.add(0, getView("musicactivity", new Intent(MainActivity.this, MusicActivity.class)));
				break;
			case 1:
				// views.remove(last);
				// manager.getActivity("musiclistactivity").onContentChanged();
				// views.add(
				// 1,
				// getView("musiclistactivity", new Intent(
				// MainActivity.this, MusicListActivity.class)));
				break;
			case 2:
				views.remove(last);
				manager.getActivity("recentactivity").onContentChanged();
				views.add(2, getView("recentactivity", new Intent(MainActivity.this, RecentActivity.class)));
				break;
			// case 3:
			// views.remove(last);
			// manager.getActivity("artistactivity").onContentChanged();
			// views.add(
			// 3,
			// getView("artistactivity", new Intent(MainActivity.this,
			// ArtistActivity.class)));
			// break;
			default:
				break;
			}

			switch (next)
			{
			case 0:
				views.remove(next);
				manager.getActivity("musicactivity").onContentChanged();
				views.add(0, getView("musicactivity", new Intent(MainActivity.this, MusicActivity.class)));
				break;
			case 1:
				// views.remove(next);
				// manager.getActivity("musiclistactivity").onContentChanged();
				// views.add(
				// 1,
				// getView("musiclistactivity", new Intent(
				// MainActivity.this, MusicListActivity.class)));
				break;
			case 2:
				views.remove(next);
				manager.getActivity("recentactivity").onContentChanged();
				views.add(2, getView("recentactivity", new Intent(MainActivity.this, RecentActivity.class)));
				break;
			// case 3:
			// views.remove(next);
			// manager.getActivity("artistactivity").onContentChanged();
			// views.add(
			// 3,
			// getView("artistactivity", new Intent(MainActivity.this,
			// ArtistActivity.class)));
			// break;
			default:
				break;
			}
			return views.get(position);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1)
		{
		}

		@Override
		public Parcelable saveState()
		{
			return null;
		}

		@Override
		public void startUpdate(View arg0)
		{
		}
	}

	private class MyOnPageChangeListener implements OnPageChangeListener
	{

		@Override
		public void onPageScrollStateChanged(int arg0)
		{
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		@Override
		public void onPageSelected(int arg0)
		{
			tabHost.setCurrentTab(arg0);
			switch (arg0)
			{
			case 0:
				((RadioButton) findViewById(R.id.radio_button0)).setChecked(true);
				break;
			case 1:
				((RadioButton) findViewById(R.id.radio_button1)).setChecked(true);
				break;
			case 2:
				((RadioButton) findViewById(R.id.radio_button2)).setChecked(true);
				break;
			// case 3:
			// ((RadioButton) findViewById(R.id.radio_button3))
			// .setChecked(true);
			// break;
			default:
				break;
			}

		}
	}
}
