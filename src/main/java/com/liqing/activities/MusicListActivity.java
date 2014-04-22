package com.liqing.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liqing.R;
import com.liqing.bean.Music;
import com.liqing.bean.MusicList;
import com.liqing.mediaplayer_music.MainActivity;
import com.liqing.musicService.MusicService;
import com.liqing.util.FileUtil;
import com.liqing.util.NetUtil;

public class MusicListActivity extends Activity implements
	android.view.View.OnClickListener {

	private ExpandableListView customlists = null;// 二级列表
	private ImageView addlist = null, imgsearch = null, groupInd = null;// 添加musiclist的点击
	private boolean isResultShow = false;

	private ArrayList<String> customlists_group_list = null;// 用户自定义播放列表一级显示数据
	private ArrayList<List<String>> customlists_child_list = null;// 用户自定义播放列表二级显示数据
	private ArrayList<String> result_name = null, result_list = null;
	private ArrayList<Integer> result_position = null;

	private LayoutInflater inflater = null;
	private EditText searchEditText = null;

	private ListView resultListView = null;
	private FrameLayout frm_search = null;

	private MusicList musicList = null;

	MyExpandableListAdapter myExpandableListAdapter = null;

	public static Handler _viewHandler = new Handler();
	
	private String musiclistToDown = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_musiclist);

		this.musicList = new MusicList(getApplicationContext());

		this.customlists = (ExpandableListView) this
				.findViewById(R.id.musiclists);
		// this.customlists.setGroupIndicator(this.getResources().getDrawable(R.drawable.musiclist_expandablelistview_selector));

		this.addlist = (ImageView) this.findViewById(R.id.addlist);
		this.imgsearch = (ImageView) this.findViewById(R.id.img_search);
		this.addlist.setOnClickListener(this);
		this.imgsearch.setOnClickListener(this);

		this.searchEditText = (EditText) this.findViewById(R.id.eidt_search);

		this.inflater = getLayoutInflater();
		this.registerForContextMenu(customlists);

		this.customlists.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				initializeData();
				myExpandableListAdapter.notifyDataSetChanged();
				return false;
			}
		});

		this.customlists.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				String listname = customlists_group_list.get(groupPosition);
				if (listname.equals("默认列表")) {
					listname = MusicList.DEFAULT_TABLE_NAME;
				}
				Intent intent = new Intent(MainActivity.SELECTED_ACTION);
				intent.putExtra(MusicService.LIST_NAME, listname);
				intent.putExtra(MusicService.ID, childPosition);
				sendBroadcast(intent);
				return false;
			}
		});

		this.frm_search = (FrameLayout) this.findViewById(R.id.frm_search);
		this.resultListView = (ListView) this.findViewById(R.id.search_result);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 初始化组、子列表数据
	 */
	private void initializeData() {
		musicList.refreshCustomListNameList();
		musicList.refreshCustomListData();

		if (this.customlists_group_list == null) {
			this.customlists_group_list = new ArrayList<String>();
		}
		if (this.customlists_child_list == null) {
			this.customlists_child_list = new ArrayList<List<String>>();
		}
		if (this.customlists_group_list.size() > 0) {
			this.customlists_group_list.clear();
		}
		if (this.customlists_child_list.size() > 0) {
			this.customlists_child_list.clear();
		}

		this.customlists_group_list.add("默认列表");
		int size = 0;
		// 添加customlists_group_list的一级列表
		if (MusicList.customListNameList != null) {
			size = MusicList.customListNameList.getCount();
			if (size > 0) {
				MusicList.customListNameList.moveToFirst();

				for (int i = 0; i < size; i++) {
					this.customlists_group_list
							.add(MusicList.customListNameList.getString(MusicList.customListNameList
									.getColumnIndex(MusicList.LIST_NAME)));
					MusicList.customListNameList.moveToNext();
				}
			}
		}

		// 添加customlists_group_list的二级列表
		if (MusicList.customListData != null) {
			int lenght = MusicList.customListData.size();
			for (int i = 0; i < lenght; i++) {
				Cursor temp = MusicList.customListData.get(i);
				if (temp != null) {
					// temp.moveToFirst();
					ArrayList<String> templist = new ArrayList<String>();
					size = temp.getCount();
					if (size > 0) {
						temp.moveToFirst();
						for (int j = 0; j < size; j++) {
							templist.add(temp.getString(temp
									.getColumnIndex(Music.TITLE_KEY)));
							temp.moveToNext();
						}
					}
					this.customlists_child_list.add(templist);
				}
			}
		}
	}

	// Activity由 全部可见可操作 变为 部分可见但不可以操作 时执行
	@Override
	public void onStart() {
		if (musicList == null) {
			musicList = new MusicList(getApplicationContext());
		}

		if (this.customlists == null) {
			this.customlists = (ExpandableListView) this
					.findViewById(R.id.musiclists);
		}

		initializeData();

		if (myExpandableListAdapter == null) {
			myExpandableListAdapter = new MyExpandableListAdapter(
					customlists_group_list, customlists_child_list);
			this.customlists.setAdapter(myExpandableListAdapter);
		}
		myExpandableListAdapter.notifyDataSetChanged();
		super.onStart();
	}

	// Activity由 部分可见但不可以操作 变为 全部可见可操作 时执行
	@Override
	public void onStop() {
		super.onStop();
	}

	// 用于从LocalActivityManager调用
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		onResume();
	}

	// Activity由 完全不可见不可以操作 变为 全部可见可操作或者部分可见不可操作 时执行
	@Override
	public void onResume() {
		// activity可见时执行的操作写在下面
		if (musicList == null) {
			musicList = new MusicList(getApplicationContext());
		}

		if (this.customlists != null) {
			initializeData();

			if (myExpandableListAdapter == null) {
				myExpandableListAdapter = new MyExpandableListAdapter(
						customlists_group_list, customlists_child_list);
				this.customlists.setAdapter(myExpandableListAdapter);
			}
			myExpandableListAdapter.notifyDataSetChanged();
		}

		super.onResume();
	}

	// Activity由 全部可见可操作或者部分可见不可操作 变为 完全不可见不可以操作 时执行
	@Override
	public void onPause() {
		super.onPause();
	}

	// 设置上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, view, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);//
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			if(group == 0 || customlists_group_list.get(group).equals(MainActivity.LIST_name))
				return;
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.layout.menutolist, menu);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			if(group == 0|| customlists_group_list.get(group).equals(MainActivity.LIST_name))
				return;
			menu.add(0, 1, 0, "play");
			menu.add(0, 2, 0, "delete");
		}
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	// ContextMenu上下文选择处理
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		ExpandableListContextMenuInfo exinfo = ((ExpandableListContextMenuInfo) menuItem
				.getMenuInfo());
		int type = ExpandableListView
				.getPackedPositionType(exinfo.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int position = (int) exinfo.id;

			switch (menuItem.getItemId()) {
			case R.id.add:
				musiclistToDown = customlists_group_list.get(position);
				Intent intent = new Intent(this, FileSystemActivity.class);
				intent.putExtra(MusicList.LIST_NAME, musiclistToDown);
				MainActivity.isPushThisToStack = true;
				this.startActivity(intent);
				break;
			case R.id.delete:
				String listname = customlists_group_list.get(position);
				MusicList.customListNameList.moveToPosition(position - 1);
				int id = MusicList.customListNameList
						.getInt(MusicList.customListNameList
								.getColumnIndex(Music.KEY_ID));
				musicList.deleteMusicList(id, listname);
				customlists_group_list.remove(position);
				myExpandableListAdapter.notifyDataSetChanged();
				break;
			case R.id.modify:
				final View view = inflater.inflate(R.layout.alertdialog, null);
				final String oldName = customlists_group_list.get(position);
				((EditText) view.findViewById(R.id.listnameedittext))
						.setHint(oldName);
				new AlertDialog.Builder(this).setView(view).setTitle("更改列表名")
						.setPositiveButton("确定", new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText editText = (EditText) view
										.findViewById(R.id.listnameedittext);
								String listname = editText.getText().toString();

								musicList.changeListName(oldName, listname);

								InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
								imm.toggleSoftInput(0,
										InputMethodManager.HIDE_NOT_ALWAYS);

								initializeData();
							}
						}).setNegativeButton("取消", new AlertDialog.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
								imm.toggleSoftInput(0,
										InputMethodManager.HIDE_NOT_ALWAYS);
							}
						}).show();
				break;
			default:
				break;
			}
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(exinfo.packedPosition);
			int childPos = ExpandableListView
					.getPackedPositionChild(exinfo.packedPosition);
			switch (menuItem.getItemId()) {
			case 1:// 播放
				Intent intent = new Intent(MainActivity.SELECTED_ACTION);
				intent.putExtra(MusicService.LIST_NAME,
						customlists_group_list.get(groupPos));
				intent.putExtra(MusicService.ID, childPos);
				sendBroadcast(intent);
				break;
			case 2:// 删除
				Cursor cursor = MusicList.customListData.get(groupPos);
				cursor.moveToPosition(childPos);
				Music music = new Music(cursor.getInt(cursor
						.getColumnIndex(Music.KEY_ID)), cursor.getString(cursor
						.getColumnIndex(Music.TITLE_KEY)), null, null, null,
						null, null, null);
				musicList.deleteMusic(music,
						customlists_group_list.get(groupPos));
				this.customlists_child_list.get(groupPos).remove(childPos);
				myExpandableListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
		return super.onContextItemSelected(menuItem);
	}

	// 添加musiclistname
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addlist:
			// 添加音乐列表
			addList();
			break;
		case R.id.img_search:
			if (!isResultShow) {
				isResultShow = true;
				imgsearch.setImageResource(R.drawable.close);
				if (searchEditText.getText().toString().trim().length() > 0) {
					// 搜索
					String str = searchEditText.getText().toString();
					final String[] datas = str.split(" ");
					if (datas.length == 2) {

						search(datas[0], datas[1]);

						ListAdapter adapter = new MyResultAdapter(result_name,
								result_list);
						this.resultListView.setAdapter(adapter);
						this.frm_search.setVisibility(View.VISIBLE);

						this.resultListView
								.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> parent, View view,
											int position, long id) {
										if (position == result_name.size() - 1) {
											// 下载
											final NetUtil netUtil = new NetUtil(
													getApplicationContext());
											netUtil.setListName(MusicList.DEFAULT_TABLE_NAME);
											netUtil.setMessage(
													datas[0],
													datas[1],
													FileUtil.SDCardRoot
															+ FileUtil.MUSICPATH
															+ datas[0] + ".mp3");
											Thread thread = new Thread(
													new Runnable() {
														@Override
														public void run() {
															// Auto-generated
															// method stub
															netUtil.downloadMusic();
															netUtil.downloadAlbumArt();
															netUtil.downloadLrc();
														}
													});
											thread.start();
										} else {
											// 播放
											Intent intent = new Intent(
													MainActivity.SELECTED_ACTION);
											intent.putExtra(
													MusicService.LIST_NAME,
													result_list.get(position));
											intent.putExtra(MusicService.ID,
													result_position
															.get(position));
											sendBroadcast(intent);
										}
										isResultShow = false;
										searchEditText.setText("");
										imgsearch
												.setImageResource(R.id.img_search);
										frm_search.setVisibility(View.GONE);
									}
								});
					} else {
						Toast.makeText(this, "请输入歌曲名 歌手的格式", Toast.LENGTH_SHORT)
								.show();
					}
				}
			} else {
				isResultShow = false;
				imgsearch.setImageResource(R.drawable.search);
				searchEditText.setText("");
				frm_search.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
	}

	private void addList() {
		final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		final LinearLayout linearLayout = (LinearLayout) this
				.findViewById(R.id.addlistEdit);
		linearLayout.setVisibility(View.VISIBLE);
		final EditText editText = (EditText) linearLayout.getChildAt(0);

		editText.requestFocus();

		final ImageView im1 = (ImageView) linearLayout.getChildAt(1);
		final ImageView im2 = (ImageView) linearLayout.getChildAt(2);

		im1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editText.getText().length() > 0) {
					musicList.addListName(editText.getText().toString());
					linearLayout.setVisibility(View.GONE);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
					customlists_group_list.add(editText.getText().toString());
					customlists_child_list.add(new ArrayList<String>());
					myExpandableListAdapter.notifyDataSetChanged();
					editText.setText("");
				}
			}
		});

		im2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				editText.setText("");
				linearLayout.setVisibility(View.GONE);
			}
		});
	}

	private void search(String musicname, String singer) {
		// 搜索
		// ArrayList<String >
		if (result_list == null) {
			result_list = new ArrayList<String>();
		}
		if (result_name == null) {
			result_name = new ArrayList<String>();
		}
		if (result_position == null) {
			result_position = new ArrayList<Integer>();
		}
		result_position.clear();
		result_name.clear();
		result_list.clear();

		// 搜索子列表
		int size = 0;
		if (MusicList.customListData != null) {
			int lenght = MusicList.customListData.size();
			for (int i = 0; i < lenght; i++) {
				Cursor temp = MusicList.customListData.get(i);
				if (temp != null) {
					// temp.moveToFirst();
					size = temp.getCount();
					if (size > 0) {
						temp.moveToFirst();
						for (int j = 0; j < size; j++) {
							String tempName = temp.getString(temp
									.getColumnIndex(Music.TITLE_KEY));
							if (tempName.contains(musicname)) {
								result_position.add(j);
								result_name.add(tempName);
								result_list.add(customlists_group_list.get(i));
							}
							temp.moveToNext();
						}
					}
				}
			}
		}
		result_position.add(0);
		result_name.add("点击下载该音乐");
		result_list.add("");
		// 显示搜索结果的列表
	}

	@Override
	public boolean onKeyDown(int keycode,KeyEvent keyEvent){
		return super.onKeyDown(keycode, keyEvent);
	}
	
	class MyExpandableListAdapter extends BaseExpandableListAdapter {

		private ArrayList<String> group;
		private ArrayList<List<String>> child;

		public MyExpandableListAdapter(ArrayList<String> group,
				ArrayList<List<String>> child) {
			this.group = group;
			this.child = child;
		}

		// -----------------Child----------------//
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			if (child == null || child.size() < 1) {
				return null;
			}
			return child.get(groupPosition).get(childPosition);
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (child == null || child.size() < 1) {
				return 0;
			}
			return child.get(groupPosition).size();
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (child == null || child.size() < 1) {
				return null;
			}
			String string = child.get(groupPosition).get(childPosition);
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.activity_musiclist_item, null);
			}
			TextView textView = (TextView) convertView
					.findViewById(R.id.musiclist_item);
			textView.setText(string);

			if (groupPosition == 0) {
				convertView.setLongClickable(false);
			}

			return convertView;
		}

		// ----------------Group----------------//
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public int getGroupCount() {
			if (group != null) {
				return group.size();
			}
			return 0;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return group.get(groupPosition);
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			String string = group.get(groupPosition);
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.activity_musiclist_group, null);
			}
			TextView textView = (TextView) convertView
					.findViewById(R.id.musiclist_group);
			textView.setText(string);
			groupInd = (ImageView) convertView.findViewById(R.id.groupind);
			groupInd.setImageResource(R.drawable.extlistview_down);
			
			if (!isExpanded) {
				groupInd.setImageResource(R.drawable.extlistview_right);
			}
			if (groupPosition == 0) {
				convertView.setLongClickable(false);
			}

			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	class MyResultAdapter extends BaseAdapter {

		private ArrayList<String> musicnames, musiclists;

		public MyResultAdapter(ArrayList<String> musicnames,
				ArrayList<String> musiclists) {
			this.musicnames = musicnames;
			this.musiclists = musiclists;
		}

		@Override
		public int getCount() {
			if (musicnames != null) {
				return this.musicnames.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(
						R.layout.activity_searchresult_item, null);

			}
			TextView txt1 = (TextView) convertView.findViewById(R.id.text1);
			TextView txt2 = (TextView) convertView.findViewById(R.id.text2);

			txt1.setText(musicnames.get(position));
			txt2.setText(musiclists.get(position));

			return convertView;
		}

	}

}
