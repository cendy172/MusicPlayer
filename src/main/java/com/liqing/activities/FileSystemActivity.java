package com.liqing.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.liqing.R;
import com.liqing.bean.Music;
import com.liqing.bean.MusicList;
import com.liqing.util.FileUtil;
import com.liqing.util.NetUtil;

public class FileSystemActivity extends Activity implements OnClickListener {

	private ArrayList<String> rootFolders, rootMusics, currentMusics,
			currentFolders = null;// 保存根目录文件夹和音乐
	private Stack<String> pathsStack = null;// 保存依次的绝对路径

	private ArrayList<Integer> selectedPositions = null;

	private static HashMap<Integer, Boolean> isSelected;

	private MyBaseAdaper myBaseAdaper1, myBaseAdaper2;

	private String listName = null;// 保存要添加到的列表名
	private MusicList musicList = null;

	private ImageView back = null;// 返回图标
	private TextView addTo = null;//
	private CheckBox allSelect = null;// 全选
	private ListView folders = null, musics = null;
	
	private int[] itemimg = { R.drawable.music_folder, R.drawable.music };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.rootFolders = new ArrayList<String>();
		this.rootMusics = new ArrayList<String>();
		this.currentFolders = new ArrayList<String>();
		this.currentMusics = new ArrayList<String>();
		this.selectedPositions = new ArrayList<Integer>();

		if (isSelected == null) {
			isSelected = new HashMap<Integer, Boolean>();
		}

		this.pathsStack = new Stack<String>();
		this.pathsStack.add(Environment.getExternalStorageDirectory()
				.toString());

		
		this.listName = this.getIntent().getStringExtra(MusicList.LIST_NAME);
		if(this.listName.equals("默认列表")){
			this.listName = MusicList.DEFAULT_TABLE_NAME;
		}

		// 扫描文件
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			getAllfiles(Environment.getExternalStorageDirectory().toString());
		}

		// 加载ListView

		// 扫描文件完毕后，正在加载显示结局，显示列表
		setContentView(R.layout.activity_filesystem);
		this.folders = (ListView) this.findViewById(R.id.folders);
		this.musics = (ListView) this.findViewById(R.id.musics);

		musicList = new MusicList(getApplicationContext());

		this.back = (ImageView) this.findViewById(R.id.back);
		this.back.setOnClickListener(this);
		this.addTo = (TextView) this.findViewById(R.id.addto);
		this.addTo.setOnClickListener(this);
		this.allSelect = (CheckBox) this.findViewById(R.id.allselecte);
		
		// 全选
		this.allSelect
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (isChecked) {
							for (int i = 0; i < isSelected.size(); i++) {
								isSelected.put(i, true);
							}

							for (int i = 0; i < musics.getChildCount(); i++) {
								View view = musics.getChildAt(i);
								CheckBox checkBox = (CheckBox) view
										.findViewById(R.id.music_selecte);
								if (!checkBox.isChecked()) {
									checkBox.setChecked(true);
									isSelected.put(i, true);
								}
							}
						} else {
							for (int i = 0; i < isSelected.size(); i++) {
								isSelected.put(i, false);
							}

							for (int i = 0; i < musics.getChildCount(); i++) {
								View view = musics.getChildAt(i);
								CheckBox checkBox = (CheckBox) view
										.findViewById(R.id.music_selecte);
								if (checkBox.isChecked()) {
									checkBox.setChecked(false);
									isSelected.put(i, false);
								}
							}
						}
						myBaseAdaper2.notifyDataSetChanged();
					}
				});

		this.folders.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String frontpath = pathsStack.get(pathsStack.size() - 1);

				if (pathsStack.size() == 1) {
					String currentPath = frontpath + "/"
							+ rootFolders.get(position);
					pathsStack.add(currentPath);
					getAllfiles(currentPath);
					setAdaper(currentFolders, currentMusics);
				} else {
					String currentPath = frontpath + "/"
							+ currentFolders.get(position);
					pathsStack.add(currentPath);
					getAllfiles(currentPath);
					setAdaper(currentFolders, currentMusics);
				}
			}
		});

		this.musics.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				if (viewHolder.music_select.isChecked()) {
					viewHolder.music_select.setSelected(false);
					isSelected.put(position, false);
				} else {
					viewHolder.music_select.setSelected(true);
					isSelected.put(position, true);
				}
				viewHolder.music_select.toggle();// 这句改变CheckBox的状态*****重要

				myBaseAdaper2.notifyDataSetChanged();
			}
		});

		setAdaper(rootFolders, rootMusics);
	}

	// 用于更新列表
	private void setAdaper(ArrayList<String> folders, ArrayList<String> musics) {
		// 如果是root,换到不是root
		TextView textView = (TextView) this.findViewById(R.id.tip);
		textView.setVisibility(View.GONE);
		if (this.addTo.isEnabled() && this.allSelect.isEnabled()) {
			this.addTo.setEnabled(false);
			this.allSelect.setEnabled(false);
		}
		isSelected.clear();
		for (int i = 0; i < musics.size(); i++) {
			isSelected.put(i, false);
		}

		myBaseAdaper1 = new MyBaseAdaper(folders, itemimg[0]);
		myBaseAdaper2 = new MyBaseAdaper(musics, itemimg[1]);
		if (folders.size() == 0 && musics.size() == 0) {
			textView.setVisibility(View.VISIBLE);
		}
		if (musics.size() != 0) {
			this.addTo.setEnabled(true);
			this.allSelect.setEnabled(true);
		}
		this.folders.setAdapter(myBaseAdaper1);
		this.musics.setAdapter(myBaseAdaper2);
	}

	private void getAllfiles(String path) {
		if (pathsStack.size() == 1 && rootFolders.size() == 0) {
			try {
				File dir = new File(path);
				File[] files = dir.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						rootFolders.add(files[i].getName());
					} else if (files[i].getName().contains(".mp3")) {
						// 把.mp3文件的名字和路径存下来
						rootMusics.add(files[i].getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (pathsStack.size() != 1) {
			currentFolders.clear();
			currentMusics.clear();
			try {
				File dir = new File(path);
				File[] files = dir.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						currentFolders.add(files[i].getName());
					} else if (files[i].getName().contains(".mp3")) {
						// 把.mp3文件的名字和路径存下来
						currentMusics.add(files[i].getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 保存到数据库
	private void saveToDatabase() {

		MusicList.getMusicList(this);// 重新得到系统的媒体数据库

		ArrayList<String> music = null;
		if (pathsStack.size() == 1) {
			music = rootMusics;
		} else {
			music = currentMusics;
		}
		Music temp = null;
		String path = pathsStack.get(pathsStack.size() - 1);
		int size = isSelected.size();
		for (int i = 0; i < size; i++) {
			if (isSelected.get(i)) {
				temp = MusicList.getMusic(this, path + "/" + music.get(i));
				String[] data = null;
				if (temp.getMusicName().equals("")) {
					temp.setMusicName(music.get(i));
					temp.setPath(path + "/" + music.get(i));
				}else if(temp.getArtist().equals("<unknown>")){
					data = temp.getMusicName().split("-");
					if(data.length == 2){
						temp.setArtist(data[0].trim());
						temp.setMusicName(data[1].trim());
					}
				}
				musicList.saveToList(temp, listName);
			}
		}
		
		// 下载这个列表里面的歌曲的专辑封面和歌词
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Cursor tempCursor = musicList
						.getListMusic(listName);

				NetUtil netUtil = new NetUtil(getApplicationContext());
				netUtil.setListName(listName);

				for (int i = 0; i < tempCursor.getCount(); i++) {
					tempCursor.moveToPosition(i);
					String tempmusicname = tempCursor
							.getString(tempCursor
									.getColumnIndex(Music.TITLE_KEY));

					String tempsinger = tempCursor.getString(tempCursor
							.getColumnIndex(Music.ARTIST));
					String target = tempCursor.getString(tempCursor
							.getColumnIndex(Music.ALBUM_ART));

					String temppath = tempCursor.getString(tempCursor
							.getColumnIndex(Music.PATH));
					netUtil.setMessage(tempmusicname, tempsinger,
							temppath);
					if (target == null
							|| target.length() < 1
							|| !FileUtil.isFileExist(target + ".png",
									FileUtil.ALBUMPATH)) {
						boolean is = NetUtil
								.isNetConnection(getApplicationContext());
						if (is) {
							netUtil.downloadAlbumArt();
						}
					}

					if (temppath != null
							&& temppath.length() > 0
							&& !FileUtil._isFileExist(temppath.replace(
									".mp3", ".lrc"))) {
						boolean is = NetUtil
								.isNetConnection(getApplicationContext());
						if (is) {
							netUtil.downloadLrc();
						}
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public boolean onKeyDown(int keycode,KeyEvent keyEvent){
		if(keycode == KeyEvent.KEYCODE_BACK){
			this.finish();
			return true;
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			if (pathsStack.size() == 1) {
				// 返回MainActivity
				this.finish();
			} else {
				// 返回Root目录，重新设置Adapter的数据，从而达到切换界面的效果
				int lastPosition = pathsStack.size() - 1;
				pathsStack.remove(lastPosition);
				getAllfiles(pathsStack.get(lastPosition - 1));
				allSelect.setChecked(false);
				if (lastPosition - 1 == 0) {
					setAdaper(rootFolders, rootMusics);
				} else {
					setAdaper(currentFolders, currentMusics);
				}
			}
			break;
		case R.id.addto:
			final ProgressDialog progressDialog = ProgressDialog.show(FileSystemActivity.this, "添加歌曲到"+listName, "正在添加，请稍候...",true);
			new Thread(){
				@Override
				public void run(){
					for (int i = 0; i < musics.getChildCount(); i++) {
						View view = musics.getChildAt(i);
						CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.music_selecte);
						if (checkBox.isChecked()) {
							selectedPositions.add(i);
						}
					}
					saveToDatabase();
					progressDialog.dismiss();
				}
			}.start();
			break;
		default:
			break;
		}
	}

	class MyBaseAdaper extends BaseAdapter {

		ArrayList<String> data;
		int img = 0;

		public MyBaseAdaper(ArrayList<String> data, int img) {

			this.data = data;
			this.img = img;
		}

		@Override
		public int getCount() {
			return this.data.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder = null;

			if (img == itemimg[0]) {
				if (convertView == null) {
					viewHolder = new ViewHolder();
					convertView = getLayoutInflater().inflate(
							R.layout.folder_item, null);
					viewHolder.folder_show = (ImageView) convertView
							.findViewById(R.id.folder_show);
					viewHolder.folder_item = (TextView) convertView
							.findViewById(R.id.folder_item);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}
				viewHolder.folder_show.setBackgroundResource(img);
				viewHolder.folder_item.setText(data.get(position));

			} else if (img == itemimg[1]) {
				if (convertView == null) {
					viewHolder = new ViewHolder();
					convertView = getLayoutInflater().inflate(
							R.layout.music_item, null);
					viewHolder.music_show = (ImageView) convertView
							.findViewById(R.id.music_show);
					viewHolder.music_item = (TextView) convertView
							.findViewById(R.id.music_item);
					viewHolder.music_select = (CheckBox) convertView
							.findViewById(R.id.music_selecte);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}
				viewHolder.music_show.setBackgroundResource(img);
				viewHolder.music_item.setText(data.get(position));
				viewHolder.music_select.setChecked(isSelected.get(position));
			}
			return convertView;
		}
	}

	final class ViewHolder {
		public ImageView music_show = null;
		public TextView music_item = null;
		public CheckBox music_select = null;
		public ImageView folder_show = null;
		public TextView folder_item = null;
	}
}
