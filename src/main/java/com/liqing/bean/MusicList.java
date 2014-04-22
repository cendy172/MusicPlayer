package com.liqing.bean;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

import com.liqing.listdatabase.ListDataBase;

public class MusicList {

	public static final String LIST_NAME = "listname";// ���ڱ����б���������,��Щ�б���ͬʱ��Ϊ������ı���
	private static volatile Cursor cursor = null;
	private static int count = 0;
	
	private static final String TABLENAME_STRING = "mymusiclist";
	public static final String DEFAULT_TABLE_NAME="default_list";
	
	private Context context = null;
	public ListDataBase listDataBase;

//	public static ArrayList<String> allMusicPathList = new ArrayList<String>();// ���и�����·���������б�
//	public static ArrayList<List<String>> allMusicPathListData = new ArrayList<List<String>>();// ·�������µ��б�
	public static Cursor customListNameList;// �����û������б�ı����б�
	public static ArrayList<Cursor> customListData = new ArrayList<Cursor>();// �����û������б�ı������б�

	public MusicList(Context context) {
		this.context = context;

		listDataBase = new ListDataBase(context);

		// listDataBase.close();
		// listDataBase.open();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		count = sharedPreferences.getInt("count", 0);

	}

	// ���������б��Cursor����Ϊstatic,�ɹ�����activity����ʹ��
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
	
	// ˢ���û��Զ��岥���б�������б�
	public void refreshCustomListNameList() {
		if(customListNameList != null && customListNameList.getCount() > 0)
		{
			customListNameList.close();
		}
		customListNameList = getAllListName();
	}

	// ˢ�������û��Զ��岥���б�ĸ����б�
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

	// �����ݿ��������
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

	// �������б�д��Ӧ�����ݿ⣬�������������б�
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
						.insertMusicList(listName, TABLENAME_STRING + count);// �򱣴����б����ͱ����ı����������
				listDataBase.createTable(TABLENAME_STRING + count);// ����һ����listname��Ӧ��tablelist���ĸ����б��
				count++;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("count", count);
				editor.commit();
				return true;
			}
		}
	}

	// �������б�
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

	// �������б�д��Ӧ�����ݿ⣬position��ϵͳ���ݿ��Cursor�У����������б���
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
				// ɾ���������һ�������һ��������ж��Ƿ����ظ�
				if (listDataBase.hasExistListName("recent",
						music.getMusicName())) {
					// �����ظ�������ظ���ɾ�����µļӵ����
					listDataBase.deleteRecord("recent",
							listDataBase.getId("recent", music.getMusicName()));
				} else {
					// �������ظ������������ӵ�ɾ��
					temp.moveToLast();// �ƶ������
					int early = temp.getInt(temp.getColumnIndex(Music.KEY_ID));
					listDataBase.deleteRecord("recent", early);
					temp.close();
					temp = null;
				}
			} else {
				boolean is = listDataBase.hasExistListName("recent",
						music.getMusicName());
				if (is) {
					// �����ظ�������ظ���ɾ��
					listDataBase.deleteRecord("recent",
							listDataBase.getId("recent", music.getMusicName()));
				}
			}
		}
		// �������²��ŵ�
		listDataBase.insertRecord(tablename, music);
	}
	
	public void updateAlbumArt(String listName,String musicname, String albumArt){
		String tableName = this.listDataBase.getTableName(listName);
		this.listDataBase.updateAlbumArt(tableName, musicname, albumArt);
	}

	// ɾ���б�
	public boolean deleteMusicList(int id, String listname) {
		String tablename = listDataBase.getTableName(listname);
		listDataBase.deleteRecord(ListDataBase.TABLE_NAME, id);// ɾ�������б��
		listDataBase.deleteTable(tablename);
		return true;
	}

	// ɾ����¼
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

	// ��ȡӦ�����ݿ���ָ���б������б�Cursor
	public Cursor getListMusic(String listName) {
		if (listDataBase == null) {
			return null;
		} else {
			if (listName.equals("recent")) {
				return listDataBase.getAllMusic("recent");// ֻ����30��������30���Ͳ�������
			} else if(listName.equals(DEFAULT_TABLE_NAME)){
				return listDataBase.getAllMusic(DEFAULT_TABLE_NAME);
			}else{
				return listDataBase.getAllMusic(listDataBase
						.getTableName(listName));
			}
		}
	}

	// ��Ӧ�����ݿ���ĳ�б��ڵ�ĳ�������ƶ�����һ�б�
	public void moveToList(Music music, String fromListName, String toListName) {
		// ��fromListNameɾ��
		Music temp = listDataBase.getOneMusic(fromListName, music.getId());
		listDataBase.deleteRecord(listDataBase.getTableName(fromListName),
				music.getId());
		// ��ӵ���toListName
		listDataBase.insertRecord(listDataBase.getTableName(toListName), temp);
	}

	// ��Ӧ�����ݿ���ĳ�б��ڵ�ĳ���������Ƶ���һ�б�
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
