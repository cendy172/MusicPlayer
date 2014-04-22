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
	 * ����URL�����ļ���
	 * @param url
	 * @return
	 */
	public static String downloadStringFromInterent(String url) {
		StringBuffer sp = null;
		String line = null;
		InputStream inputstream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader buffer = null;//ʹ��IO����ȡ����
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
					buffer.close();//�ر�IO��
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
	 * �ú����������� -1�����������ļ����� 0�����������ļ��ɹ� 1�������ļ��Ѿ�����
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
		URL url = null;//URL����
		HttpURLConnection urlconn = null;//HTTP��ַ���Ӷ���
		url = new URL(urlStr);//newһ��URL����
		urlconn = (HttpURLConnection) url.openConnection();//��һ�����Ӷ���
		urlconn.setConnectTimeout(6000);//�������ӳ�ʱ
		urlconn.setReadTimeout(6000);//���ö��볬ʱ
		urlconn.setRequestMethod("GET");//��������ģʽ
		return urlconn.getInputStream();
	}
}
