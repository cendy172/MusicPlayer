package com.liqing.bean;

public class Music {

	/**
	 * ���г������ص����ݷ���·��Ϊ��
	 * ר������:/mnt/sdcard/musicplayer/album/ר����.png
	 * ����ͷ��/mnt/sdcard/musicplayer/singer/������.png
	 * ���֣�/mnt/sdcard/musicplayer/music/������-����.mp3
	 */
	
	public final static String KEY_ID = "_id";// ������������ΪSimpleCursorAdapterֻʶ��_id������
	public final static String TITLE_KEY = "title_key";// ����
	public final static String DURATION = "duration";// ʱ��
	public final static String BOOKMARK = "bookmark";// ��󲥷�ʱ�� ms
	public final static String ARTIST = "artist";// ����
	public final static String COMPOSER = "composer";// ������
	
	/**
	 * The album the audio file is from, if any
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public final static String ALBUM = "album";// ר��

	/**
	 * A URI to the album art, if any
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public final static String ALBUM_ART = "album_art";//���ڱ���ר������ͼƬ������

	public final static String PATH = "path";// ·��

	private int id = 0;
	private String musicName = null;
	private String duration = null;
	private String artist = null;
	private String composer = null;
	private String album = null;
	private String albumart = null;
	private String path = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAlbumart() {
		return albumart;
	}

	public void setAlbumart(String albumart) {
		this.albumart = albumart;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Music(){
		
	}
	
	public Music(int id, String musicName, String duration, String artist,
			String composer, String album, String albumart, String path) {
		super();
		this.id = id;
		this.musicName = musicName;
		this.duration = duration;
		this.artist = artist;
		this.composer = composer;
		this.album = album;
		this.albumart = albumart;//ר����
		this.path = path;
	}

	public Music(int id,String musicName, String duration, String artist,
			String album, String path , String albumart) {
		super();
		this.id = id;
		this.musicName = musicName;
		this.duration = duration;
		this.artist = artist;
		this.albumart = albumart;
		this.album = album;
		this.path = path;
	}
	
	public Music(String musicName, String duration, String artist,
			String album, String path,String albumart) {
		super();
		this.musicName = musicName;
		this.duration = duration;
		this.artist = artist;
		this.albumart = albumart;
		this.album = album;
		this.path = path;
	}
	

	/**
	 * ʱ���ʽת��
	 * @param time
	 * @return
	 */
	public static String toTime(int time) {
        
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		if(hour != 0){
			return String.format("%02d:%02d:%02d", hour,minute,second);
		}
		return String.format("%02d:%02d", minute, second);
	}

	
	
}
