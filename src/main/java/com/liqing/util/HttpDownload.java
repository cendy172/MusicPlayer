package com.liqing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownload {
	
	/**
	 * 根据URL下载文件。
	 * @param url
	 * @return
	 */
	public static String downloadStringFromInterent(String url) {
		StringBuffer sp = null;
		String line = null;
		InputStream inputstream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader buffer = null;//使用IO流读取数据
		try {
			inputstream = getInputStreamFromUrl(url);
			inputStreamReader = new InputStreamReader(inputstream);
			buffer = new BufferedReader(inputStreamReader);
			sp = new StringBuffer();
			while ((line = buffer.readLine()) != null) {
				sp.append(line.trim());
			}
			return sp.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();//关闭IO流
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (inputstream != null) {
						try {
							inputstream.close();
						} catch (IOException e) {
				
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 该函数返回整形 -1：代表下载文件出错 0：代表下载文件成功 1：代表文件已经存在
	 */
	public int downLoadFile(String urlStr, String path, String finalFileName) {
		InputStream inputStream = null;
		try {
			if (FileUtil.isFileExist(finalFileName + ".lrc", path)) {
				FileUtil.deleteFile(path, finalFileName + ".lrc");
			}
			inputStream = getInputStreamFromUrl(urlStr);
			File resultFile = FileUtil.writeToSDFromInput(path+ finalFileName
					+ ".lrc", inputStream);
			if (resultFile == null) {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	private static InputStream getInputStreamFromUrl(String urlStr)
			throws IOException {
		URL url = null;//URL对象
		HttpURLConnection urlconn = null;//HTTP地址连接对象
		url = new URL(urlStr);//new一个URL对象
		urlconn = (HttpURLConnection) url.openConnection();//打开一个连接对象
		urlconn.setConnectTimeout(6000);//设置连接超时
		urlconn.setReadTimeout(6000);//设置读秒超时
		urlconn.setRequestMethod("GET");//设置请求模式
		return urlconn.getInputStream();
	}
}
