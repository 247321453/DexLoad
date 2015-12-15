package com.example.testplugn.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class FileUtils {

	public static final String TAG = "MyProvider";
	public static final String IFUNBOWSDK = Combine(getStoragePath(), "download/cache/");

	public static String getStoragePath() {
		try {
			String str = Environment.getExternalStorageDirectory().getAbsolutePath();
			return str;
		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return "";
	}

	public static List<String> FindApk(String filedir) {
		List<String> list = new ArrayList<String>();
		File[] files = new File(filedir).listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.getName().endsWith(".apk")) {
				String f = file.getName();
				list.add(f);
			}
		}
		return list;
	}

	public static String getFileName(String file) {
		if (file == null)
			return "";
		if (file.endsWith("/"))
			file = file.substring(0, file.length() - 1);
		int i = file.lastIndexOf("/");
		return (i >= 0) ? file.substring(i + 1) : file;
	}

	public static String Combine(String... paths) {
		if (paths == null || paths.length == 0) {
			return "";
		} else {
			StringBuilder builder = new StringBuilder();
			String spliter = File.separator;
			String firstPath = paths[0];
			if ("http".equals(firstPath.toLowerCase(Locale.US))) {
				spliter = "/";
			}
			if (!firstPath.endsWith(spliter)) {
				firstPath = firstPath + spliter;
			}
			builder.append(firstPath);
			for (int i = 1; i < paths.length; i++) {
				String nextPath = paths[i];
				if (nextPath.startsWith("/") || nextPath.startsWith("\\")) {
					nextPath = nextPath.substring(1);
				}
				if (i != paths.length - 1) // not the last one
				{
					if (nextPath.endsWith("/") || nextPath.endsWith("\\")) {
						nextPath = nextPath.substring(0, nextPath.length() - 1) + spliter;
					} else {
						nextPath = nextPath + spliter;
					}
				}
				builder.append(nextPath);
			}
			return builder.toString();
		}
	}

	public static String getFilePath(String url) {
		return FileUtils.Combine(IFUNBOWSDK, FileUtils.getFileName(url));
	}

	public static boolean isExists(String file) {
		if (file == null || file.length() == 0)
			return false;
		return new File(file).exists();
	}

	public static boolean deleteFile(String fileName) {
		try {
			File ff = new File(fileName);
			if (ff.exists()) {
				ff.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public static boolean CopyFromAsset(Context context, String FileName, String destName) {
		boolean isOK = false;
		if (TextUtils.isEmpty(FileName) || TextUtils.isEmpty(destName) || !createDir(getDirByFile(destName)))
			return isOK;
		InputStream inputstream = null;
		try {
			inputstream = context.getAssets().open(FileName);
			isOK = writeFile(inputstream, destName);
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
		return isOK;
	}

	public static String getDirByFile(String file) {
		if (TextUtils.isEmpty(file))
			return "";
		int i = file.lastIndexOf("/");
		return (i > 0) ? file.substring(0, i + 1) : "/";
	}

	public static boolean createDir(String dirname) {
		if (TextUtils.isEmpty(dirname))
			return false;
		File localFile = new File(dirname);
		if (localFile.isFile())
			return false;
		if (!localFile.exists())
			return localFile.mkdirs();
		return true;
	}

	public static boolean writeFile(InputStream InputStream, String destFile) throws IOException {
		if (TextUtils.isEmpty(destFile))
			return false;
		return writeFile(InputStream, new File(destFile));
	}

	public static boolean writeFile(InputStream InputStream, File destFile) throws IOException {
		if (destFile.exists()) {
			destFile.delete();
		}
		destFile.createNewFile();
		FileOutputStream OutputStream;
		OutputStream = new FileOutputStream(destFile);
		byte[] readBytes = new byte[4096];
		int i = -1;
		while ((i = InputStream.read(readBytes)) > 0) {
			OutputStream.write(readBytes, 0, i);
		}
		OutputStream.close();
		return true;
	}

}
