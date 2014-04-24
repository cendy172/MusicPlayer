package com.liqing.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理歌词文件的类
 */
public class LrcProcess {

	private ArrayList<LrcContent> LrcList;

	private LrcContent mLrcContent;

	public LrcProcess() {
		mLrcContent = new LrcContent();
		LrcList = new ArrayList<LrcContent>();
	}

	/**
	 * 读取歌词文件的内容
	 */
	public String readLRC(String song_path) {
		// public void Read(String file){

		LrcList.clear();

		StringBuilder stringBuilder = new StringBuilder();

		File f = new File(song_path.replace(".mp3", ".lrc"));

		try {
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis, "GB2312");
			BufferedReader br = new BufferedReader(isr);
			String s = "";
			while ((s = br.readLine()) != null) {
				
				// 替换字符
				s = s.replace("]", "]@");

				// 分离"@"字符
				String splitLrc_data[] = s.split("@");

				int count = splitLrc_data.length;
				
				if (count > 0) {
					String content = "";
					if(isTime(splitLrc_data[count - 1])){
						content = ""; //这行只有时间没有内容
					}else{
						content = splitLrc_data[count-1]; 
					}
					for (int i = 0; i < count; i++) {
						
						mLrcContent.setLrc(content);
						
						if(isTime(splitLrc_data[i])){//有时间才将内容和时间加进歌词
							splitLrc_data[i] = splitLrc_data[i].replace("[", "");
							splitLrc_data[i] = splitLrc_data[i].replace("]", "");
							int LrcTime = TimeStr(splitLrc_data[i]);
							mLrcContent.setLrc_time(LrcTime);
							LrcList.add(mLrcContent);
							mLrcContent = new LrcContent();
						}
					}
				}
			}
			if(LrcList.size() > 0){
				Mycomparator mycomparator = new Mycomparator();
				Collections.sort(LrcList, mycomparator);//对歌词进行排序，按照时间先后排序
			}
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			stringBuilder.append("木有歌词文件，赶紧去下载！...");
		} catch (IOException e) {
			e.printStackTrace();
			stringBuilder.append("木有读取到歌词啊！");
		}
		return stringBuilder.toString();
	}

	/**
	 * 解析歌曲时间处理类
	 */
	public int TimeStr(String timeStr) {

		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@");

		// 分离出分、秒并转换为整型
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// 计算上一行与下一行的时间转换为毫秒数
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;

		return currentTime;
	}

	/**
	 * 判断字符串是否为歌词的时间，格式可以有三种00:00.00或者00:00或者00:00:00
	 * @param str 被判断的字符串
	 * @return
	 */
	private boolean isTime(String str){
		Pattern pattern = Pattern.compile("\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,2})\\]|\\[(\\d{1,2}:\\d{1,2}\\:\\d{1,2})\\]|\\[(\\d{1,2}:\\d{1,2})\\]");
		Matcher isMatcher = pattern.matcher(str);
		if(isMatcher.matches()){
			return true;
		}
		return false;
	}
	
	public ArrayList<LrcContent> getLrcContent() {
		return LrcList;
	}

	/**
	 * 获得歌词和时间并返回的类
	 */
	public class LrcContent {
		private String Lrc;
		private int Lrc_time;

		public String getLrc() {
			return Lrc;
		}

		public void setLrc(String lrc) {
			Lrc = lrc;
		}

		public int getLrc_time() {
			return Lrc_time;
		}

		public void setLrc_time(int lrc_time) {
			Lrc_time = lrc_time;
		}
	}

	public class Mycomparator implements Comparator {
		@Override
		public int compare(Object o1, Object o2) {
			LrcContent l1 = (LrcContent) o1;
			LrcContent l2 = (LrcContent) o2;
			return l1.Lrc_time - l2.Lrc_time;
		}
	}
}
