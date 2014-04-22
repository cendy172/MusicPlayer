package com.liqing.util;

import android.os.Environment;

import java.io.*;

public class FileUtil
{

	public final static String ALBUMPATH = "musicplayer/album/";// ���ص�ר���б��·��
	public final static String SINGERPATH = "musicplayer/singer/";// ���صĸ���ͷ���·��
	public final static String MUSICPATH = "musicplayer/music/";// ���ص����ִ洢��·��

	/**
	 * �õ���ǰ�ⲿ�洢�豸��Ŀ¼
	 */
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	public static File createFileInSDCard(String fileName, String dir) throws IOException
	{
		if (isFileExist(fileName, dir))
		{
			deleteFile(dir, fileName);
		}
		File file = new File(SDCardRoot + dir + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * ��SD���ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	public static File createFileInSDCard(String absPath) throws IOException
	{
		if (_isFileExist2(absPath))
		{
			deleteAnotherFile(absPath);
		}
		File file = new File(absPath);
		file.createNewFile();
		return file;
	}

	/**
	 * ��SD���ϴ���Ŀ¼
	 * 
	 * @param dirName
	 */
	public static File creatSDDir(String dir)
	{
		File dirFile = new File(SDCardRoot + dir);
		dirFile.mkdirs();
		return dirFile;
	}

	/**
	 * ɾ��SD���ϵ��ļ�
	 * 
	 * @param dir
	 *            �ļ���Ŀ¼(�Ѱ�����Ŀ¼)
	 * @param fileName
	 *            �ļ���
	 * @return
	 * @throws IOException
	 */
	public static boolean deleteFile(String dir, String fileName) throws IOException
	{
		File dirFile = new File(SDCardRoot + dir + fileName);
		return dirFile.delete();
	}

	/**
	 * ɾ��_data·�����ļ�
	 * 
	 * @param _data
	 * @return
	 */
	public static boolean deleteAnotherFile(String _data) throws IOException
	{
		File dirFile = new File(_data);
		return dirFile.delete();
	}

	public static boolean isFileNoData(String filename, String path)
	{
		File file = new File(SDCardRoot + path + filename);
		if (file.length() == 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * �ж�SD���ϵ��ļ��Ƿ����
	 */
	public static boolean isFileExist(String fileName, String path)
	{
		File file = new File(SDCardRoot + path + fileName);
		return file.exists();
	}

	public static boolean _isFileExist(String fileName)
	{
		File file = new File(SDCardRoot + "mp3" + File.separator + fileName);
		return file.exists();
	}

	/**
	 * 
	 * @param path
	 *            ·�����ļ����Ӻ�׺
	 * @return
	 */
	public static boolean _isFileExist2(String path)
	{
		File file = new File(path);
		return file.exists();
	}

	/**
	 * �ж�SD���ϵ��ļ����Ƿ����
	 */
	public static boolean isFilePathExist(String path)
	{
		File file = new File(SDCardRoot + path);
		return file.exists();
	}

	/**
	 * �ж�SD���ϵ��ļ����Ƿ����
	 */
	public static boolean _isFilePathExist(String abspath)
	{
		File file = new File(abspath);
		return file.exists();
	}

	public static String getFilePath(String fileName)
	{
		return SDCardRoot + "mp3" + File.separator + fileName;
	}

	/**
	 * �����ļ���
	 * 
	 * @param file
	 * @param finalFileName
	 */
	public static void updateFileName(File file, String finalFileName)
	{
		String path = file.getAbsolutePath();
		String orgFileName = path.substring(0, path.lastIndexOf("/") + 1) + finalFileName;
		file.renameTo(new File(orgFileName));
	}

	/**
	 * ��һ��д�������������д�뵽SD����
	 */
	public static File writeToSDFromInput(String abspath, InputStream inPutStream)
	{
		File file = null;
		OutputStream outPutStream = null;
		try
		{
			file = createFileInSDCard(abspath);
			outPutStream = new FileOutputStream(file);// ��һ��file����д������
			byte buffer[] = new byte[10 * 1024];
			int temp;
			while ((temp = inPutStream.read(buffer)) != -1)
			{
				outPutStream.write(buffer, 0, temp);// ÿ����һ��
			}
			outPutStream.flush();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (outPutStream != null)
				{
					outPutStream.close();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return file;
	}

}
