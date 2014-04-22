package com.liqing.listdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.liqing.bean.Music;
import com.liqing.bean.MusicList;

public class ListDataBase {

	// ���ݿ���
	public static final String DB_NAME = "ListDatabase.db";

	// ����
	public static final String KEY_ID = "_id"; // Ψһ��ID

	public static final String LIST_NAME = "listname";// ���ڱ����б���������,��Щ�б���ͬʱ��Ϊ������ı���

	public static final String TABLE_NAME = "ListName";
	public static final String DEFAULT_TABLE_NAME = "default_list";

	private static final int DB_VERSION = 1;

	// ��������б�����ݿ�
	private SQLiteDatabase listDatabase;
	private ContentValues contentValues = null;

	// ����Context����
	private Context context = null;

	// �����������ݿ�Ķ���
	private static ListSQLiteOpenHelper listSQLiteOpenHelper = null;

	/** ����ģʽ **/
	static synchronized ListSQLiteOpenHelper getInstance(Context context) {
		if (listSQLiteOpenHelper == null) {
			listSQLiteOpenHelper = new ListSQLiteOpenHelper(context);
		}
		return listSQLiteOpenHelper;
	}

	private static class ListSQLiteOpenHelper extends SQLiteOpenHelper {

		public ListSQLiteOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		/* ����һ���� */
		@Override
		public void onCreate(SQLiteDatabase db) {
			// �ȴ���һ��������tablename�ֶ��Ƕ�Ӧ�б����ı���,
			String createTable = "create table " + TABLE_NAME + " ( " + KEY_ID
					+ " integer primary key , " + LIST_NAME + " text unique, "
					+ "tablename" + " text unique" + " )";
			db.execSQL(createTable);
			// ����һ��������ű�
			createTable = "create table recent ( " + KEY_ID
					+ " integer primary key, " + Music.TITLE_KEY + " text, "
					+ Music.ARTIST + " text, " + Music.PATH + " text, "
					+ Music.DURATION + " text, " + Music.ALBUM_ART + " text ) ";
			db.execSQL(createTable);

			// ����һ��Ĭ���б����ó������صĸ���
			createTable = "create table " + DEFAULT_TABLE_NAME + " ( " + KEY_ID
					+ " integer primary key, " + Music.TITLE_KEY + " text, "
					+ Music.ARTIST + " text, " + Music.PATH + " text, "
					+ Music.DURATION + " text, " + Music.ALBUM + " text, "
					+ Music.ALBUM_ART + " text ) ";
			db.execSQL(createTable);
		}

		/* �������ݿ� */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists notes");
			onCreate(db);
		}
	}

	/* ���캯��-ȡ��Context */
	public ListDataBase(Context context) {
		this.context = context;
		this.contentValues = new ContentValues();
		open();
	}

	// �����ݿ⣬�������ݿ����
	public void open() {
		listSQLiteOpenHelper = getInstance(context);
		this.listDatabase = listSQLiteOpenHelper.getWritableDatabase();
	}

	// �ر����ݿ�
	public void close() {
		if (listSQLiteOpenHelper != null) {
			listSQLiteOpenHelper.close();
			listSQLiteOpenHelper = null;
		}
	}

	// ������
	public void createTable(String tableName) {
		String create = "create table " + tableName + " ( " + KEY_ID
				+ " integer primary key, " + Music.TITLE_KEY + " text, "
				+ Music.ARTIST + " text, " + Music.PATH + " text, "
				+ Music.DURATION + " text, " + Music.ALBUM + " text, "
				+ Music.ALBUM_ART + " text ) ";
		this.listDatabase.execSQL(create);
	}

	// ����б���
	public void insertMusicList(String listname, String tablename) {
		contentValues.put(LIST_NAME, listname);
		contentValues.put("tablename", tablename);
		this.listDatabase.insert(TABLE_NAME, null, contentValues);
		contentValues.clear();
	}

	// ��Ӽ�¼
	public void insertRecord(String tableName, Music music) {
		contentValues.put(Music.TITLE_KEY, music.getMusicName());
		contentValues.put(Music.ARTIST, music.getArtist());
		contentValues.put(Music.PATH, music.getPath());
		contentValues.put(Music.DURATION, music.getDuration());
		contentValues.put(Music.ALBUM_ART, music.getAlbumart());

		if (!tableName.equals("recent")) {
			contentValues.put(Music.ALBUM, music.getAlbum());
		}
//		this.listDatabase.insertOrThrow(tableName, null, contentValues);
		this.listDatabase.insert(tableName, null, contentValues);
		contentValues.clear();
	}

	// ɾ����
	public void deleteTable(String tableName) {
		this.listDatabase.execSQL("drop table " + tableName);
	}

	// ɾ����¼
	public boolean deleteRecord(String tableName, int id) {
		boolean isDelete = this.listDatabase.delete(tableName, Music.KEY_ID
				+ "=?", new String[] { String.valueOf(id) }) > 0;
		return isDelete;
	}

	// �鿴�����б���
	public Cursor getAllListName() {
		Cursor temp = this.listDatabase.query(TABLE_NAME, null, null, null,
				null, null, Music.KEY_ID);
		return temp;
	}

	public String getTableName(String listName) {
		String result = null;
		if (listName.equals("recent")) {
			result = listName;
		} else if(listName.equals(MusicList.DEFAULT_TABLE_NAME)){
			result = MusicList.DEFAULT_TABLE_NAME;
		}else {
			Cursor cursor = this.listDatabase.query(TABLE_NAME,
					new String[] { "tablename" }, LIST_NAME + "=?",
					new String[] { listName }, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				result = cursor.getString(0);
				cursor.close();
			}
		}
		return result;
	}

	// ���һ�м�¼
	public Music getOneMusic(String listname, int id) {
		Cursor temp = listDatabase.query(getTableName(listname), null,
				Music.KEY_ID + "=?", new String[] { String.valueOf(id) }, null,
				null, null);
		if (temp != null && temp.getCount() > 0) {
			Music music = new Music(temp.getInt(temp
					.getColumnIndex(Music.KEY_ID)), temp.getString(temp
					.getColumnIndex(Music.TITLE_KEY)), temp.getString(temp
					.getColumnIndex(Music.DURATION)), temp.getString(temp
					.getColumnIndex(Music.ARTIST)), temp.getString(temp
					.getColumnIndex(Music.ALBUM)), temp.getString(temp
					.getColumnIndex(Music.PATH)), temp.getString(temp
					.getColumnIndex(Music.ALBUM_ART)), temp.getString(temp
					.getColumnIndex(Music.ALBUM_ART)));
			return music;
		}
		return null;
	}

	// �鿴ĳ�������еĸ���
	public Cursor getAllMusic(String tablename) {
		Cursor cursor = null;
		if (tablename.equals("recent")) {
			cursor = this.listDatabase.query(tablename, null, null, null, null,
					null, Music.KEY_ID + " desc ");
		} else {
			cursor = this.listDatabase.query(tablename, null, null, null, null,
					null, null);
		}
		return cursor;
	}

	public int getId(String listname, String name) {
		if (listname.equals("recent")) {
			Cursor temp = listDatabase.query(listname, null, Music.TITLE_KEY
					+ "=?", new String[] { name }, null, null, null);
			temp.moveToFirst();
			int result = temp.getInt(temp.getColumnIndex(Music.KEY_ID));
			// temp.close();
			// temp = null;
			return result;
		}
		return 0;
	}

	// �鿴ĳ�б��Ƿ���ڻ���ĳ���Ƿ����
	public boolean hasExistListName(String listname, String name) {
		String tablename = null;
		Cursor cursor = null;
		if(listname.equals(MusicList.DEFAULT_TABLE_NAME)){
			tablename = listname;
			cursor = this.listDatabase.query(tablename, null, Music.TITLE_KEY + "=?",
					new String[] { name }, null, null, null);
		}
		if (listname.equals(TABLE_NAME)) {
			// �б��
			tablename = TABLE_NAME;
			cursor = this.listDatabase.query(tablename, null, LIST_NAME + "=?",
					new String[] { name }, null, null, null);
		} else if (listname.equals("recent")) {
			// ��������б�
			tablename = "recent";
			cursor = this.listDatabase.query(tablename, null, Music.TITLE_KEY
					+ "=?", new String[] { name }, null, null, null);
		} else {
			tablename = getTableName(listname);
			cursor = this.listDatabase.query(tablename, null, Music.TITLE_KEY
					+ "=?", new String[] { name }, null, null, null);
		}

		return cursor.getCount() > 0;
	}

	// �����б���
	public boolean updateListName(String fromName, String toName) {
		contentValues.put(LIST_NAME, toName);
		int result = this.listDatabase.update(TABLE_NAME, contentValues,
				LIST_NAME + "=?", new String[] { fromName });
		contentValues.clear();
		return result > 0;
	}

	public boolean updateAlbumArt(String tableName, String musicName,
			String AlbumArt) {
		contentValues.put(Music.ALBUM_ART, AlbumArt);
		int result = this.listDatabase.update(tableName, contentValues,
				Music.TITLE_KEY + "=?", new String[] { musicName });
		contentValues.clear();
		return result > 0;
	}

}
