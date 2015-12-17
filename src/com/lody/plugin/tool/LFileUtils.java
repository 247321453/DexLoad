package com.lody.plugin.tool;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by lody on 2015/4/4. 文件工具类
 * @hide
 */
public class LFileUtils {

	/**
	 * 将流写入文件
	 *
	 * @param dataIns
	 * @param target
	 * @throws java.io.IOException
	 */
	public static void writeToFile(InputStream dataIns, File target)
			throws IOException {
		final int BUFFER = 1024;
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(target));
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = dataIns.read(data, 0, BUFFER)) != -1) {
			bos.write(data, 0, count);
		}
		bos.close();
	}

	/**
	 * 从字节数组中写入文件
	 * 
	 * @param data
	 * @param target
	 * @throws java.io.IOException
	 */
	public static void writeToFile(byte[] data, File target) throws IOException {
		FileOutputStream fo = null;
		ReadableByteChannel src = null;
		FileChannel out = null;
		try {
			src = Channels.newChannel(new ByteArrayInputStream(data));
			fo = new FileOutputStream(target);
			out = fo.getChannel();
			out.transferFrom(src, 0, data.length);
		} finally {
			if (fo != null) {
				fo.close();
			}
			if (src != null) {
				src.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public static void copyAsset(Context context, String name, String target) {
		if (TextUtils.isEmpty(target)) {
			return;
		}
		File file = null;
		try {
			file = new File(target);
		} catch (Exception e) {
			e.printStackTrace();
		}
		copyAsset(context, name, file);
	}

	public static void copyAsset(Context context, String name, File target) {
		if (target == null || TextUtils.isEmpty(name)) {
			return;
		}
		File dir = target.getParentFile();
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}

		InputStream inputstream = null;
		try {
			inputstream = context.getAssets().open(name);
			writeToFile(inputstream, target);
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
	public static void copyFile(File source, File target) {
		try {
			copyFile(new FileInputStream(source), target);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 复制文件
	 *
	 * @param source
	 *            - 源文件
	 * @param target
	 *            - 目标文件
	 */
	public static void copyFile(FileInputStream fi, File target) {
		if (target == null) {
			return;
		}
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		try {
			// fi = new FileInputStream(source);
			fo = new FileOutputStream(target);
			File dir = target.getParentFile();
			if (dir != null && !dir.exists()) {
				dir.mkdirs();
			}
			in = fi.getChannel();// 得到对应的文件通道
			out = fo.getChannel();// 得到对应的文件通道
			in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
