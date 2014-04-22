package com.liqing.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;

import com.liqing.bean.Music;
import com.liqing.bean.MusicList;

/**
 * @author liqing
 * 
 *         用于下载网络上的歌曲，歌词，专辑列表，头像
 * 
 *         专辑封面在soso下载，歌曲和歌词还有头像通过baidu下载
 */

public class NetUtil {

	private String musicName;// 歌曲名
	private String singer;// 歌手
	private String musicEncode;// GBK转码后的musicName值的字符串，因为要URL中显示
	private String singerEncode;// GBK转码后的singer值的字符串，因为要URL中显示
	private String musicPath;// 歌曲路径，绝对路径，包括文件名

	private String listName;// 当前下载的专辑封面的歌曲所在播放列表
	private Context context = null;// 用于操作MusicList对象

	/**
	 * 设置musicName
	 * 
	 * @param musicName
	 *            歌曲名
	 * @param singer
	 *            歌手名
	 * @param musicPath
	 *            歌曲的绝对路径
	 */
	public void setMessage(String musicName, String singer, String musicPath) {
		this.musicName = musicName;
		this.singer = singer;
		this.musicPath = musicPath;
	}

	/**
	 * 用于将专辑封面名称加入数据库
	 * 
	 * @param listName
	 *            当前下载的专辑封面的歌曲所在播放列表
	 */
	public void setListName(String listName) {
		this.listName = listName;
	}

	/**
	 * 构造函数
	 */
	public NetUtil(Context context) {
		super();
		this.context = context;
		// FileUtil.creatSDDir(FileUtil.ALBUMPATH);
		// FileUtil.creatSDDir(FileUtil.SINGERPATH);
		// FileUtil.creatSDDir(FileUtil.MUSICPATH);
	}

	/**
	 * 判断是否联网
	 * 
	 * @param Context
	 *            上下文
	 * @return Boolean 是否联网
	 */
	public static boolean isNetConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// 因为要使用Jsoup解析获得的Html文件，在不是wifi的情况下要设置代理，设置代理的过程必须放在Jsoup.connect方法使用之前
				String host = Proxy.getDefaultHost();
				int port = Proxy.getDefaultPort();
				if (host != null && port != -1) {
					System.getProperties().setProperty("proxySet", "true");
					System.setProperty("http.proxyHost", "true");
					System.setProperty("http.proxyPort", Integer.toString(port));
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 获得专辑封面
	 * 
	 * @return Bitmap 专辑封面的Bitmap
	 */
	public Bitmap getAlbumArt() {
		Bitmap bitmap = null;

		if (!FileUtil.isFileExist(musicName + ".png", FileUtil.ALBUMPATH)) {
			downloadAlbumArt();
		}

		// bitmap = BitmapFactory.decodeFile(FileUtil.SDCardRoot
		// + FileUtil.ALBUMPATH + musicName + ".png");

		return bitmap;
	}

	/**
	 * 下载专辑封面 通过一听获得歌曲获得init_src属性的值，将90改为150即为所需图片的地址，默认下载第一个
	 * 地址：http://so.1ting.com/all.do?q=想幸福的人+杨丞琳
	 */
	public void downloadAlbumArt() {
		// 下载并保存,将专辑名存入对应歌曲的数据库中
		try {
			this.musicEncode = URLEncoder.encode(this.musicName, "UTF-8");
			this.singerEncode = URLEncoder.encode(singer, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		String AlbumArtURL = "http://so.1ting.com/all.do?q=" + this.musicEncode
				+ "+" + this.singerEncode;

		String title = null;
		String tempsrc = null;

		try {
			Document document = Jsoup.connect(AlbumArtURL).timeout(600000)
					.get();
			Elements elements = document.select("td[class=album] a[href]");

			if (elements != null && elements.size() > 0) {
				Element element = elements.first();
				title = element.text();// 获得的专辑名
				tempsrc = element.attr("href");
				if (title.length() != 0) {
					MusicList musicList = new MusicList(context);
					if(listName.equals(MusicList.DEFAULT_TABLE_NAME)){
						musicList.updateAlbumArt(MusicList.DEFAULT_TABLE_NAME, musicName, title);
					}else{
						musicList.updateAlbumArt(listName, musicName, title);
					}
				}
			}

			if (tempsrc != null && tempsrc.length() > 0) {
				String src = null;
				Document document1 = Jsoup.connect(tempsrc).timeout(60000)
						.get();
				Elements elements1 = document1
						.select("div[class=albumPic_300]");
				src = elements1.select("img").attr("src");
				save(FileUtil.SDCardRoot + FileUtil.ALBUMPATH + title + ".png",
						getInputStreamFromUrl(src));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得歌手的头像
	 * 
	 * @return Bitmap 歌手头像的Bitmap
	 */
	public Bitmap getSingerBitmap() {
		Bitmap bitmap = null;
		if (!FileUtil.isFileExist(singer + ".png", FileUtil.SINGERPATH)) {
			downloadSingerBitmap();
		}
		bitmap = BitmapFactory.decodeFile(FileUtil.SDCardRoot
				+ FileUtil.SINGERPATH + File.separator + singer + ".png");
		return bitmap;
	}

	/**
	 * 下载歌手头像 通过百度获得百度百科的地址，从百度百科中获得歌手头像的地址
	 * http://www.baidu.com/s?wd=王心凌&rsv_spt
	 * =1&issp=1&rsv_bp=0&ie=utf-8&tn=baiduhome_pg&inputT=1422
	 * 然后获得百度百科的地址，再获得歌手头像
	 */
	private void downloadSingerBitmap() {
		// 下载歌手头像并保存
//		String SingerURL = "http://www.baidu.com/s?wd="
//				+ this.singerEncode
//				+ "&rsv_spt=1&issp=1&rsv_bp=0&ie=utf-8&tn=baiduhome_pg&inputT=1422";
	}

	/**
	 * 下载歌词
	 * http://mp3.baidu.com/m?word=musicname+singer&lm=-1&f=ms&tn=baidump3&ct
	 * =134217728&lf=&rn= 获取第一个链接！再次访问即为歌词的地址 歌词和歌曲放在同路径下，名字也一样，只是更改后缀为.lrc
	 */
	public void downloadLrc() {
		if(FileUtil._isFileExist2(musicPath.replace(".mp3", ".lrc"))){
			return;//没起作用
		}
		try {
			this.musicEncode = URLEncoder.encode(this.musicName, "GBK");
			this.singerEncode = URLEncoder.encode(this.singer, "GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// 下载歌词并保存
		// String LrcURL = "http://mp3.baidu.com/m?word="
		// + this.musicEncode + "+" + this.singerEncode +
		// "&lm=-1&f=ms&tn=baidump3&ct=134217728&lf=&rn=";

		String LrcURL = "http://mp3.easou.com/l.e?actType=1&q="
				+ this.musicEncode + "+" + this.singerEncode;

		String lrcURL = null;

		try {
			String link1 = null;
			Document document = Jsoup.connect(LrcURL).timeout(60000).get();

			Elements allurls = document.select("div[class=frame] a[href]");// 歌曲下载链接
			if (allurls != null && allurls.size() > 0) {
				Element element = allurls.first();
				link1 = element.attr("href");
				if (link1 != null && link1.length() > 0) {
					link1 = "http://mp3.easou.com"+link1;
					
					Document document1 = Jsoup.connect(link1).timeout(60000)
							.get();
					Elements elements = document1.select("a[href]");
					if (elements != null && elements.size() > 0) {
						for(Element e:elements){
							if(e.text().contains("下载LRC歌词")){
								lrcURL = e.attr("href");
								break;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (lrcURL != null && lrcURL.length() > 0) {
			String savePath = this.musicPath.replace(".mp3", ".lrc");
			// 下载歌词并保存
				try {
					this.save(savePath, getInputStreamFromUrl(lrcURL));
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 下载歌曲，还要加入数据库
	 * http://mp3.baidu.com/d?song=彩虹&singer=五月天&album=&appendix=&size
	 * =4718592&cat=0&attr=0 获取下载地址，然后再下载 歌曲格式为 歌手名-文件名.mp3
	 */
	public void downloadMusic() {
		try {
			this.musicEncode = URLEncoder.encode(this.musicName, "GBK");
			this.singerEncode = URLEncoder.encode(this.singer, "GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// 下载歌曲并保存
		String MusicURL = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
				+ this.musicEncode + "$$" + this.singerEncode + "$$$$";
		String musicURL = null;
		
		String encode = null,decode = null;
		try {
			Document document = Jsoup.connect(MusicURL).timeout(60000).get();
			Elements encodes = document.select("encode");
			if(encodes != null && encodes.size()>0){
				Element element = encodes.first();
				encode = element.text();
			}
			Elements decodes = document.select("decode");
			if(decodes != null && decodes.size()>0){
				Element element = decodes.first();
				decode = element.text();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(encode != null && encode.length()>0){
			if(decode != null && decode.length()>0){
				String[] datas = encode.split("/");
				musicURL = datas[0];
				for(int i = 1;i<datas.length;i++){
					if(i == datas.length - 1){
						datas[i] = decode;
					}
					musicURL += "/";
					musicURL += datas[i];
				}
			}else{
				musicURL = encode;
			}
		}
		
		// 添加到数据库,默认列表，default
		if(musicURL != null && musicURL.length()>0){
			MusicList musicList = new MusicList(context);
			Music music = new Music();
			music.setMusicName(musicName);
			music.setArtist(singer);
			music.setPath(FileUtil.SDCardRoot + FileUtil.MUSICPATH + musicName+".mp3");
			
			try {
				this.save(FileUtil.SDCardRoot + FileUtil.MUSICPATH + musicName + ".mp3", getInputStreamFromUrl(musicURL));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			File mp3 = new File(FileUtil.SDCardRoot + FileUtil.MUSICPATH + musicName + ".mp3");
			MP3File f = null;
			try {
				f = (MP3File)AudioFileIO.read(mp3);
			} catch (CannotReadException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TagException e) {
				e.printStackTrace();
			} catch (ReadOnlyFileException e) {
				e.printStackTrace();
			} catch (InvalidAudioFrameException e) {
				e.printStackTrace();
			}
			MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();
			int length = audioHeader.getTrackLength();	
			music.setDuration(String.valueOf(length)+"000");//得到的是秒为单位的时间，而我要的是毫秒为单位的时间
			
			musicList.saveToList(music, MusicList.DEFAULT_TABLE_NAME);
		}
	}

	/**
	 * 通过下载链接，获得输入流
	 * 
	 * @param urlStr
	 *            下载链接
	 * @return InputStream 输入流
	 * @throws IOException
	 */
	private static InputStream getInputStreamFromUrl(String urlStr)
			throws IOException {
		URL url = null;// URL对象
		HttpURLConnection urlconn = null;// HTTP地址连接对象
		url = new URL(urlStr);// new一个URL对象
		urlconn = (HttpURLConnection) url.openConnection();// 打开一个连接对象
		urlconn.setConnectTimeout(6000);// 设置连接超时
		urlconn.setReadTimeout(6000);// 设置读秒超时
		urlconn.setRequestMethod("GET");// 设置请求模式
		return urlconn.getInputStream();
	}

	/**
	 * 保存文件
	 * 
	 * @param name
	 *            保存的文件名
	 * @param inputStream
	 *            网络流
	 */
	private void save(String absPath, InputStream inputStream) {
		if(FileUtil._isFileExist2(absPath)){
			return;
		}
		FileUtil.writeToSDFromInput(absPath, inputStream);
	}
}
