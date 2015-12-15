package com.example.testplugn.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

	private static MD5Utils instance = null;
	private static MessageDigest md5 = null;
	private static MessageDigest sha = null;

	private MD5Utils() {
		try {
			md5 = MessageDigest.getInstance("MD5");
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	};

	public static MD5Utils getInstance() {
		instance = new MD5Utils();
		return instance;
	}

	public String getSHA(String val) {
		try {
			sha.update(val.getBytes());
			byte[] m = sha.digest();// 加密
			// return getshaString(m);
			return toHexString(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSecuFile(String filePath) {
		if (filePath == null) {
			return null;
		}
		int indexStart = filePath.lastIndexOf("/") + 1;
		int indexEnd = filePath.length() - 4;
		if (indexStart == filePath.length()) {
			return filePath;
		}
		String s1 = filePath.substring(0, indexStart);
		String str = filePath.substring(indexStart, indexEnd);
		String s2 = getSHA(str);
		String s3 = filePath.substring(indexEnd, filePath.length());
		return s1 + s2 + s3;
	}

	public String getStringHash(String source) {
		String hash = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(
					source.getBytes());
			hash = getStreamHash(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * 获取文件的MD5
	 * */
	public String getFileHash(String file) {
		String hash = null;
		try {
			FileInputStream in = new FileInputStream(file);
			hash = getStreamHash(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	public String getStreamHash(InputStream stream) {
		String hash = null;
		byte[] buffer = new byte[1024];
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(stream);
			int numRead = 0;
			while ((numRead = in.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			in.close();
			hash = toHexString(md5.digest());
		} catch (Exception e) {
			if (in != null)
				try {
					in.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			e.printStackTrace();
		}
		return hash;
	}

	private String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	private char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f' };

}
