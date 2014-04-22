package com.liqing.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.liqing.R;
import com.liqing.bean.Music;
import com.liqing.mediaplayer_music.MainActivity;
import com.liqing.musicService.MusicService;
import com.liqing.util.PreferenceKeys;
import com.liqing.views.LrcView;
import com.liqing.views.MusicView;

public class MusicActivity extends Activity implements OnClickListener,OnTouchListener {

	public int count = 0;
	int rawX=0;
	int rawy=0;
	public static MusicView musicView = null;
	public static LrcView lrcView = null;
	public static SeekBar progress = null;
	public static boolean down=false;
	public static boolean isdraw=true;
	public static boolean drawline=false;
	public static boolean drawchi=true;
	public static boolean ischangstate=false;

	public static TextView currentTime = null, endTime = null,
			musicName = null, singer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);

		FrameLayout frameLayout = (FrameLayout) this
				.findViewById(R.id.framelayout);

		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.width = this.getWindowManager().getDefaultDisplay()
				.getWidth();
		layoutParams.height = this.getWindowManager().getDefaultDisplay()
				.getWidth();
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

		musicView = new MusicView(this);
		musicView.setLayoutParams(layoutParams);
		frameLayout.addView(musicView);
		lrcView = new LrcView(this);
		lrcView.setLayoutParams(layoutParams);
		frameLayout.addView(lrcView);
		lrcView.setOnTouchListener(this);

		currentTime = (TextView) this.findViewById(R.id.currentTime);
		endTime = (TextView) this.findViewById(R.id.endTime);
		musicName = (TextView) this.findViewById(R.id.name);
		singer = (TextView) this.findViewById(R.id.singer);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		musicName.setText(sharedPreferences.getString("musicname", "歌曲名"));
		singer.setText(sharedPreferences.getString("singer", "歌手"));
		endTime.setText(Music.toTime(Integer.valueOf(sharedPreferences
				.getString("duration", "0"))));
		musicView.refreshBitmap(sharedPreferences.getString(PreferenceKeys.ALBUMART, null));

		progress = (SeekBar) this.findViewById(R.id.progress);
		progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				isdraw=false;
				down=false;
				progress.setProgress(seekBar.getProgress());
				Intent intent = new Intent(MainActivity.SEEKBAR_ACTION);
				int pro = seekBar.getProgress()
						* Integer.valueOf(MusicService.duration) / 100;
				intent.putExtra("current", pro);
				sendBroadcast(intent);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
                down=true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		// this.onSaveInstanceState(myState);
		super.onPause();
	}

	// 用于从LocalActivityManager调用
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
	}
	
	@Override
	public boolean onKeyDown(int keycode,KeyEvent keyEvent){
		return super.onKeyDown(keycode, keyEvent);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(MusicService.isNoLrc()){
			return true;
		}
		
		if(event.getAction()==MotionEvent.ACTION_MOVE)
		{
			int changX=Math.abs((int)event.getX()-rawX);
			int changY=Math.abs((int)event.getY()-rawy);
			
			if(changY-changX>20)
			{
				changY=(int)event.getY()-rawy;
				isdraw=false;
				drawchi=false;
				drawline=true;
				ischangstate=true;
				
				lrcView.setY(changY);
				lrcView.postInvalidate();
			}
			return true;
		}else if (event.getAction()==MotionEvent.ACTION_UP) {
			
			isdraw=true;
			drawchi=true;
			drawline=false;
			ischangstate=false;
			
			if(Math.abs(event.getY() - rawy) < 50){
				return true;
			}
			
			MusicActivity.progress.setProgress(lrcView.gettime() * 100 / Integer.valueOf(MusicService.duration));
			Intent intent = new Intent(MainActivity.SEEKBAR_ACTION);
			intent.putExtra("current", lrcView.gettime());
			sendBroadcast(intent);
			return true;
		}else if(event.getAction()==MotionEvent.ACTION_DOWN){
			rawX=(int) event.getX();
			rawy=(int) event.getY();
			
			return true;
		}
		
		return false;
	}

}
