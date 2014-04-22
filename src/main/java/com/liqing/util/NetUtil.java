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
 *         �������������ϵĸ�������ʣ�ר���б�ͷ��
 * 
 *         ר��������soso���أ������͸�ʻ���ͷ��ͨ��baidu����
 */

public class NetUtil {

	private String musicName;// ������
	private String singer;// ����
	private String musicEncode;// GBKת����musicNameֵ���ַ�������ΪҪURL����ʾ
	private String singerEncode;// GBKת����singerֵ���ַ�������ΪҪURL����ʾ
	private String musicPath;// ����·��������·���������ļ���

	private String listName;// ��ǰ���ص�ר������ĸ������ڲ����б�
	private Context context = null;// ���ڲ���MusicList����

	/**
	 * ����musicName
	 * 
	 * @param musicName
	 *            ������
	 * @param singer
	 *            ������
	 * @param musicPath
	 *            �����ľ���·��
	 */
	public void setMessage(String musicName, String singer, String musicPath) {
		this.musicName = musicName;
		this.singer = singer;
		this.musicPath = musicPath;
	}

	/**
	 * ���ڽ�ר���������Ƽ������ݿ�
	 * 
	 * @param listName
	 *            ��ǰ���ص�ר������ĸ������ڲ����б�
	 */
	public void setListName(String listName) {
		this.listName = listName;
	}

	/**
	 * ���캯��
	 */
	public NetUtil(Context context) {
		super();
		this.context = context;
		// FileUtil.creatSDDir(FileUtil.ALBUMPATH);
		// FileUtil.creatSDDir(FileUtil.SINGERPATH);
		// FileUtil.creatSDDir(FileUtil.MUSICPATH);
	}

	/**
	 * �ж��Ƿ�����
	 * 
	 * @param Context
	 *            ������
	 * @return Boolean �Ƿ�����
	 */
	public static boolean isNetConnection(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				// ��ΪҪʹ��Jsoup������õ�Html�ļ����ڲ���wifi�������Ҫ���ô������ô���Ĺ��̱������Jsoup.connect����ʹ��֮ǰ
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
	 * ���ר������
	 * 
	 * @return Bitmap ר�������Bitmap
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
	 * ����ר������ ͨ��һ����ø������init_src���Ե�ֵ����90��Ϊ150��Ϊ����ͼƬ�ĵ�ַ��Ĭ�����ص�һ��
	 * ��ַ��http://so.1ting.com/all.do?q=���Ҹ�����+��ة��
	 */
	public void downloadAlbumArt() {
		// ���ز�����,��ר���������Ӧ���������ݿ���
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
				title = element.text();// ��õ�ר����
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
	 * ��ø��ֵ�ͷ��
	 * 
	 * @return Bitmap ����ͷ���Bitmap
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
	 * ���ظ���ͷ�� ͨ���ٶȻ�ðٶȰٿƵĵ�ַ���ӰٶȰٿ��л�ø���ͷ��ĵ�ַ
	 * http://www.baidu.com/s?wd=������&rsv_spt
	 * =1&issp=1&rsv_bp=0&ie=utf-8&tn=baiduhome_pg&inputT=1422
	 * Ȼ���ðٶȰٿƵĵ�ַ���ٻ�ø���ͷ��
	 */
	private void downloadSingerBitmap() {
		// ���ظ���ͷ�񲢱���
//		String SingerURL = "http://www.baidu.com/s?wd="
//				+ this.singerEncode
//				+ "&rsv_spt=1&issp=1&rsv_bp=0&ie=utf-8&tn=baiduhome_pg&inputT=1422";
	}

	/**
	 * ���ظ��
	 * http://mp3.baidu.com/m?word=musicname+singer&lm=-1&f=ms&tn=baidump3&ct
	 * =134217728&lf=&rn= ��ȡ��һ�����ӣ��ٴη��ʼ�Ϊ��ʵĵ�ַ ��ʺ͸�������ͬ·���£�����Ҳһ����ֻ�Ǹ��ĺ�׺Ϊ.lrc
	 */
	public void downloadLrc() {
		if(FileUtil._isFileExist2(musicPath.replace(".mp3", ".lrc"))){
			return;//û������
		}
		try {
			this.musicEncode = URLEncoder.encode(this.musicName, "GBK");
			this.singerEncode = URLEncoder.encode(this.singer, "GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// ���ظ�ʲ�����
		// String LrcURL = "http://mp3.baidu.com/m?word="
		// + this.musicEncode + "+" + this.singerEncode +
		// "&lm=-1&f=ms&tn=baidump3&ct=134217728&lf=&rn=";

		String LrcURL = "http://mp3.easou.com/l.e?actType=1&q="
				+ this.musicEncode + "+" + this.singerEncode;

		String lrcURL = null;

		try {
			String link1 = null;
			Document document = Jsoup.connect(LrcURL).timeout(60000).get();

			Elements allurls = document.select("div[class=frame] a[href]");// ������������
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
							if(e.text().contains("����LRC���")){
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
			// ���ظ�ʲ�����
				try {
					this.save(savePath, getInputStreamFromUrl(lrcURL));
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * ���ظ�������Ҫ�������ݿ�
	 * http://mp3.baidu.com/d?song=�ʺ�&singer=������&album=&appendix=&size
	 * =4718592&cat=0&attr=0 ��ȡ���ص�ַ��Ȼ�������� ������ʽΪ ������-�ļ���.mp3
	 */
	public void downloadMusic() {
		try {
			this.musicEncode = URLEncoder.encode(this.musicName, "GBK");
			this.singerEncode = URLEncoder.encode(this.singer, "GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// ���ظ���������
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
		
		// ��ӵ����ݿ�,Ĭ���б�default
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
			music.setDuration(String.valueOf(length)+"000");//�õ�������Ϊ��λ��ʱ�䣬����Ҫ���Ǻ���Ϊ��λ��ʱ��
			
			musicList.saveToList(music, MusicList.DEFAULT_TABLE_NAME);
		}
	}

	/**
	 * ͨ���������ӣ����������
	 * 
	 * @param urlStr
	 *            ��������
	 * @return InputStream ������
	 * @throws IOException
	 */
	private static InputStream getInputStreamFromUrl(String urlStr)
			throws IOException {
		URL url = null;// URL����
		HttpURLConnection urlconn = null;// HTTP��ַ���Ӷ���
		url = new URL(urlStr);// newһ��URL����
		urlconn = (HttpURLConnection) url.openConnection();// ��һ�����Ӷ���
		urlconn.setConnectTimeout(6000);// �������ӳ�ʱ
		urlconn.setReadTimeout(6000);// ���ö��볬ʱ
		urlconn.setRequestMethod("GET");// ��������ģʽ
		return urlconn.getInputStream();
	}

	/**
	 * �����ļ�
	 * 
	 * @param name
	 *            ������ļ���
	 * @param inputStream
	 *            ������
	 */
	private void save(String absPath, InputStream inputStream) {
		if(FileUtil._isFileExist2(absPath)){
			return;
		}
		FileUtil.writeToSDFromInput(absPath, inputStream);
	}
}
