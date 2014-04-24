package com.liqing.util;

import android.os.Environment;

import java.io.*;

public class FileUtil
{

	public final static String ALBUMPATH = "musicplayer/album/";// 下载的专辑列表的路径
	public final static String SINGERPATH = "musicplayer/singer/";// 下载的歌手头像的路径
	public final static String MUSICPATH = "musicplayer/music/";// 下载的音乐存储的路径

	/**
	 * 得到当前外部存储设备的目录
	 */
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

	/**
	 * 在SD卡上创建文件
	 * 
	 * @throws java.io.IOException
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
	 * 在SD卡上创建文件
	 * 
	 * @throws java.io.IOException
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
	 * 在SD卡上创建目录
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
	 * 删除SD卡上的文件
	 * 
	 * @param dir
	 *            文件夹目录(已包含根目录)
	 * @param fileName
	 *            文件名
	 * @return
	 * @throws java.io.IOException
	 */
	public static boolean deleteFile(String dir, String fileName) throws IOException
	{
		File dirFile = new File(SDCardRoot + dir + fileName);
		return dirFile.delete();
	}

	/**
	 * 删除_data路径的文件
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
	 * 判断SD卡上的文件是否存在
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
	 *            路径加文件名加后缀
	 * @return
	 */
	public static boolean _isFileExist2(String path)
	{
		File file = new File(path);
		return file.exists();
	}

	/**
	 * 判断SD卡上的文件夹是否存在
	 */
	public static boolean isFilePathExist(String path)
	{
		File file = new File(SDCardRoot + path);
		return file.exists();
	}

	/**
	 * 判断SD卡上的文件夹是否存在
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
	 * 更新文件名
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
	 * 将一个写入流里面的数据写入到SD卡中
	 */
	public static File writeToSDFromInput(String abspath, InputStream inPutStream)
	{
		File file = null;
		OutputStream outPutStream = null;
		try
		{
			file = createFileInSDCard(abspath);
			outPutStream = new FileOutputStream(file);// 传一个file对象写入流里
			byte buffer[] = new byte[10 * 1024];
			int temp;
			while ((temp = inPutStream.read(buffer)) != -1)
			{
				outPutStream.write(buffer, 0, temp);// 每读出一行
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
