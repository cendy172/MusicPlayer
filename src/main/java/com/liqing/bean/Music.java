package com.liqing.bean;

public class Music {

	/**
	 * 所有程序下载的内容放在路径为：
	 * 专辑封面:/mnt/sdcard/musicplayer/album/专辑名.png
	 * 歌手头像：/mnt/sdcard/musicplayer/singer/歌手名.png
	 * 音乐：/mnt/sdcard/musicplayer/music/歌手名-歌名.mp3
	 */
	
	public final static String KEY_ID = "_id";// 用作主键，因为SimpleCursorAdapter只识别_id的主键
	public final static String TITLE_KEY = "title_key";// 歌名
	public final static String DURATION = "duration";// 时长
	public final static String BOOKMARK = "bookmark";// 最后播放时间 ms
	public final static String ARTIST = "artist";// 歌手
	public final static String COMPOSER = "composer";// 作曲家
	
	/**
	 * The album the audio file is from, if any
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public final static String ALBUM = "album";// 专辑

	/**
	 * A URI to the album art, if any
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public final static String ALBUM_ART = "album_art";//用于保存专辑封面图片的名称

	public final static String PATH = "path";// 路径

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
		this.albumart = albumart;//专辑名
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
	 * 时间格式转换
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
