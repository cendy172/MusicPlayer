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
 * �������ļ�����
 */
public class LrcProcess {

	private ArrayList<LrcContent> LrcList;

	private LrcContent mLrcContent;

	public LrcProcess() {
		mLrcContent = new LrcContent();
		LrcList = new ArrayList<LrcContent>();
	}

	/**
	 * ��ȡ����ļ�������
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
				
				// �滻�ַ�
				s = s.replace("]", "]@");

				// ����"@"�ַ�
				String splitLrc_data[] = s.split("@");

				int count = splitLrc_data.length;
				
				if (count > 0) {
					String content = "";
					if(isTime(splitLrc_data[count - 1])){
						content = ""; //����ֻ��ʱ��û������
					}else{
						content = splitLrc_data[count-1]; 
					}
					for (int i = 0; i < count; i++) {
						
						mLrcContent.setLrc(content);
						
						if(isTime(splitLrc_data[i])){//��ʱ��Ž����ݺ�ʱ��ӽ����
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
				Collections.sort(LrcList, mycomparator);//�Ը�ʽ������򣬰���ʱ���Ⱥ�����
			}
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			stringBuilder.append("ľ�и���ļ����Ͻ�ȥ���أ�...");
		} catch (IOException e) {
			e.printStackTrace();
			stringBuilder.append("ľ�ж�ȡ����ʰ���");
		}
		return stringBuilder.toString();
	}

	/**
	 * ��������ʱ�䴦����
	 */
	public int TimeStr(String timeStr) {

		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@");

		// ������֡��벢ת��Ϊ����
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// ������һ������һ�е�ʱ��ת��Ϊ������
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;

		return currentTime;
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��ʵ�ʱ�䣬��ʽ����������00:00.00����00:00����00:00:00
	 * @param str ���жϵ��ַ���
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
	 * ��ø�ʺ�ʱ�䲢���ص���
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
