package com.liqing.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.liqing.R;
import com.liqing.bean.Music;
import com.liqing.bean.MusicList;
import com.liqing.mediaplayer_music.MainActivity;
import com.liqing.musicService.MusicService;

public class RecentActivity extends ListActivity {

	private MusicList musicList = null;
	private Cursor recent = null;
	private SimpleCursorAdapter simpleCursorAdapter = null;
	Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent);
		if(musicList == null){
			musicList = new MusicList(getApplicationContext());
		}
	}

	// ������������ʹ��Activity�ڿɼ�ʱ����

	// ��Service
	@Override
	public void onStart() {
		super.onStart();
		initView();// ˢ���б�
	}

	// ���Service
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	// ���ڴ�LocalActivityManager����
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		onResume();
	}
	
	@Override
	public boolean onKeyDown(int keycode,KeyEvent keyEvent){
		return super.onKeyDown(keycode, keyEvent);
	}

	// �ɼ�ʱ����
	@Override
	public void onResume() {
		if(musicList == null){
			musicList = new MusicList(getApplicationContext());
		}
		
		recent = musicList.getListMusic("recent");
		
		if (simpleCursorAdapter == null) {
			simpleCursorAdapter = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_2, recent, new String[] {
							Music.TITLE_KEY, Music.ARTIST }, new int[] {
							android.R.id.text1, android.R.id.text2 });
		} else {
			simpleCursorAdapter.changeCursor(recent);
		}
		this.setListAdapter(simpleCursorAdapter);
		simpleCursorAdapter.notifyDataSetChanged();

		super.onResume();
	}

	// ���ɼ�ʱ����
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	public void initView() {
		recent = musicList.getListMusic("recent");
		simpleCursorAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, recent, new String[] {
						Music.TITLE_KEY, Music.ARTIST }, new int[] {
						android.R.id.text1, android.R.id.text2 });
		this.setListAdapter(simpleCursorAdapter);
	}
	
	@Override
	public void onListItemClick(ListView listView, View v, int position, long id) {
		// ����ѡ����
		playMusic(position);
	}
	
	private void playMusic(int id){
		Intent intent = new Intent(MainActivity.SELECTED_ACTION);
		recent.moveToPosition(id);
		intent.putExtra(MusicService.LIST_NAME, "recent");
		intent.putExtra(MusicService.ID, id);
		this.sendBroadcast(intent);
	}
}
