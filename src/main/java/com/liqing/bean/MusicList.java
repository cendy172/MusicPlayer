package com.liqing.bean;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import com.liqing.listdatabase.ListDataBase;

import java.util.ArrayList;

public class MusicList {

	public static final String LIST_NAME = "listname";// 用于保存列表名的列名,这些列表名同时作为其他表的表名
	private static volatile Cursor cursor = null;
	private static int count = 0;
	
	private static final String TABLENAME_STRING = "mymusiclist";
	public static final String DEFAULT_TABLE_NAME="default_list";
	
	private Context context = null;
	public ListDataBase listDataBase;

//	public static ArrayList<String> allMusicPathList = new ArrayList<String>();// 所有歌曲的路径分类名列表
//	public static ArrayList<List<String>> allMusicPathListData = new ArrayList<List<String>>();// 路径分类下的列表
	public static Cursor customListNameList;// 所有用户创建列表的表名列表
	public static ArrayList<Cursor> customListData = new ArrayList<Cursor>();// 所有用户创建列表的表名的列表

	public MusicList(Context context) {
		this.context = context;

		listDataBase = new ListDataBase(context);

		// listDataBase.close();
		// listDataBase.open();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		count = sharedPreferences.getInt("count", 0);

	}

	// 持有所有列表的Cursor并且为static,可供所有activity方便使用
	public static Cursor getMusicList(Context context) {
		if (cursor == null) {
			synchronized (MusicList.class) {
				if (cursor == null) {
					cursor = context.getContentResolver().query(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
							null, null,
							MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				}
			}
		}
		return cursor;
	}

	public void refreshAllList() {
		cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}

	// public void refreshAllMusicPathList() {
	//
	// allMusicPathList = context.getContentResolver().query(
	// MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	// new String[] { MediaStore.Audio.Media.DATA },
	// " 1=1) group by(" + MediaStore.Audio.Media.DATA, null,
	// MediaStore.Audio.Media.DATA);
	//
	// int lenght = allMusicPathList.getCount();
	// return;
	// }

	// public void refreshAllMusicPathListData() {
	// allMusicPathList.moveToFirst();
	// while (allMusicPathList.moveToNext()) {
	// allMusicPathListData.add(context.getContentResolver().query(
	// MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	// null,
	// MediaStore.Audio.Media.DATA + "=?",
	// new String[] { allMusicPathList.getString(allMusicPathList
	// .getColumnIndex(MediaStore.Audio.Media.DATA)) },
	// null));
	// }
	// }

	public static Music getMusic(Context context, String path){
		ContentResolver contentResolver = context.getContentResolver();
		Music music = new Music();
		Cursor temp = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaColumns.DATA + "=?", new String[]{path}, null);
		if(temp != null && temp.getCount()>0){
			temp.moveToFirst();
			music.setMusicName(temp.getString(temp.getColumnIndex(MediaColumns.TITLE)));
			music.setDuration(temp.getString(temp.getColumnIndex(AudioColumns.DURATION)));
			music.setArtist(temp.getString(temp.getColumnIndex(AudioColumns.ARTIST)));
			music.setPath(temp.getString(temp.getColumnIndex(MediaColumns.DATA)));
			music.setAlbum(temp.getString(temp.getColumnIndex(AudioColumns.ALBUM)));
		}
		return music;
	}
	
	// 刷新用户自定义播放列表的名字列表
	public void refreshCustomListNameList() {
		if(customListNameList != null && customListNameList.getCount() > 0)
		{
			customListNameList.close();
		}
		customListNameList = getAllListName();
	}

	// 刷新所有用户自定义播放列表的歌曲列表
	public void refreshCustomListData() {
		if(customListData != null && customListData.size() > 0){
			customListData.clear();
		}
		customListData.add(getListMusic(DEFAULT_TABLE_NAME));
		int size = customListNameList.getCount();
		if (size > 0) {
			customListNameList.moveToFirst();
			
			for (int i = 0;i < size; i++ ) {
				
				customListData
						.add(getListMusic(customListNameList
								.getString(customListNameList
										.getColumnIndex(LIST_NAME))));
				customListNameList.moveToNext();
			}
		}
	}

	// 从数据库读入数据
	public static Cursor RefreshAllMusicList(Context context) {
		if (cursor == null) {
			synchronized (MusicList.class) {
				if (cursor == null) {
					cursor = context.getContentResolver().query(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
							null, null,
							MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				}
			}
		}
		return cursor;
	}

	// 将数据列表写入应用数据库，创建新命名的列表
	public boolean addListName(String listName) {
		if (listDataBase == null) {
			return false;
		} else {
			if (listDataBase
					.hasExistListName(ListDataBase.TABLE_NAME, listName)) {
				return false;
			} else {
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(context);
				count = sharedPreferences.getInt("count", 1);
				listDataBase
						.insertMusicList(listName, TABLENAME_STRING + count);// 向保存了列表名和表名的表中添加数据
				listDataBase.createTable(TABLENAME_STRING + count);// 创建一个与listname对应的tablelist名的歌曲列表表
				count++;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("count", count);
				editor.commit();
				return true;
			}
		}
	}

	// 重命名列表
	public boolean changeListName(String oldName, String newName) {
		if (listDataBase == null) {
			return false;
		} else {
			if (listDataBase.hasExistListName(ListDataBase.TABLE_NAME, newName)) {
				return false;
			} else {
				listDataBase.updateListName(oldName, newName);
				return true;
			}
		}
	}

	// 将数据列表写入应用数据库，position是系统数据库的Cursor行，新命名的列表名
	public void saveToList(Music music, String listName) {
		String tablename = null;
		if(listName.equals(DEFAULT_TABLE_NAME)){
			tablename = listName;
		}
		if (!listName.equals("recent")) {
			tablename = listDataBase.getTableName(listName);
		} else {
			tablename = "recent";
			Cursor temp = listDataBase.getAllMusic("recent");
			if (temp.getCount() == 20) {
				// 删除最上面的一条，添加一条在最后，判断是否有重复
				if (listDataBase.hasExistListName("recent",
						music.getMusicName())) {
					// 存在重复，则把重复的删除，新的加到最后
					listDataBase.deleteRecord("recent",
							listDataBase.getId("recent", music.getMusicName()));
				} else {
					// 不存在重复，则把最早添加的删除
					temp.moveToLast();// 移动到最后
					int early = temp.getInt(temp.getColumnIndex(Music.KEY_ID));
					listDataBase.deleteRecord("recent", early);
					temp.close();
					temp = null;
				}
			} else {
				boolean is = listDataBase.hasExistListName("recent",
						music.getMusicName());
				if (is) {
					// 存在重复，则把重复的删除
					listDataBase.deleteRecord("recent",
							listDataBase.getId("recent", music.getMusicName()));
				}
			}
		}
		// 加入最新播放的
		listDataBase.insertRecord(tablename, music);
	}
	
	public void updateAlbumArt(String listName,String musicname, String albumArt){
		String tableName = this.listDataBase.getTableName(listName);
		this.listDataBase.updateAlbumArt(tableName, musicname, albumArt);
	}

	// 删除列表
	public boolean deleteMusicList(int id, String listname) {
		String tablename = listDataBase.getTableName(listname);
		listDataBase.deleteRecord(ListDataBase.TABLE_NAME, id);// 删除保存列表的
		listDataBase.deleteTable(tablename);
		return true;
	}

	// 删除记录
	public boolean deleteMusic(Music music, String listname) {
		String tablename = null;
		if (listname != "recent") {
			tablename = listDataBase.getTableName(listname);
		} else {
			tablename = "recent";
		}
		listDataBase.deleteRecord(tablename, music.getId());
		return true;
	}

	public Cursor getAllListName() {
		Cursor temp = listDataBase.getAllListName();
		return temp;
	}

	// 读取应用数据库中指定列表名的列表Cursor
	public Cursor getListMusic(String listName) {
		if (listDataBase == null) {
			return null;
		} else {
			if (listName.equals("recent")) {
				return listDataBase.getAllMusic("recent");// 只保存30个，超过30个就不保存了
			} else if(listName.equals(DEFAULT_TABLE_NAME)){
				return listDataBase.getAllMusic(DEFAULT_TABLE_NAME);
			}else{
				return listDataBase.getAllMusic(listDataBase
						.getTableName(listName));
			}
		}
	}

	// 将应用数据库中某列表内的某条歌曲移动到另一列表
	public void moveToList(Music music, String fromListName, String toListName) {
		// 从fromListName删除
		Music temp = listDataBase.getOneMusic(fromListName, music.getId());
		listDataBase.deleteRecord(listDataBase.getTableName(fromListName),
				music.getId());
		// 添加到表toListName
		listDataBase.insertRecord(listDataBase.getTableName(toListName), temp);
	}

	// 将应用数据库中某列表内的某条歌曲复制到另一列表
	public void copyToList(Music music, String fromListName, String toListName) {
		Music temp = listDataBase.getOneMusic(fromListName, music.getId());
		listDataBase.insertRecord(listDataBase.getTableName(toListName), temp);
	}

	//
	public boolean hasExistListName(String listname, String name){
		if(listname.equals(DEFAULT_TABLE_NAME) || listname.equals("recent")){
			if(listDataBase.hasExistListName(listname, name)){
				return true;
			}else{
				return false;
			}
		}else{
			listname = listDataBase.getTableName(listname);
			if(listDataBase.hasExistListName(listname, name)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	public void closeDatabase() {
		listDataBase.close();
	}
}
