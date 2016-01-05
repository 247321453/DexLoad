package com.lody.plugin.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.lody.plugin.BuildConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by lody on 2015/4/4.
 *
 * 根据CPU型号解压apk内的相应的.so文件
 */
@SuppressLint("DefaultLocale")
public class NativeLibUnpacker {

	private static final String DEF_ARCH_1 = "armeabi";
    private static final String DEF_ARCH_2 = "armeabi-v7a";
    private static final String DEF_ARCH_3 = "x86";

    private static String ARCH = System.getProperty("os.arch");

	public static void deleteSOFromApk(String apkPath, String toPath) {
		try {
			ZipFile apk = new ZipFile(new File(apkPath));
			int libs = deleteLibFile(apk, new File(toPath));
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "remove .so files by plugin:" + libs);
		} catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "" + e);
        }
	}

    public static void unPackSOFromApk(String apkPath, String toPath) {
        if (BuildConfig.DEBUG)
            Log.i(LPluginConfig.TAG, "CPU is " + ARCH);
		try {
			ZipFile apk = new ZipFile(new File(apkPath));
			boolean hasLib = extractLibFile(apk, new File(toPath));
            if (BuildConfig.DEBUG) {
                if (hasLib) {
                    Log.i(LPluginConfig.TAG, "The plugin is contains .so files.");
                } else {
                    Log.i(LPluginConfig.TAG, "The plugin isn't contain any .so files.");
                }
            }
		} catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,""+e);
        }

	}

	private static int deleteLibFile(ZipFile zip, File to) {

//		Map<String, List<ZipEntry>> archLibEntries = new HashMap<String, List<ZipEntry>>();
		String sys = ARCH.toLowerCase();
		if(sys.equals("i686")){
			sys = "x86";
		}else if(sys.equals(DEF_ARCH_2)){
			sys = DEF_ARCH_2;
		}else {
			sys = DEF_ARCH_1;
		}
		List<ZipEntry> libEntries = new ArrayList<ZipEntry>();
		for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			ZipEntry entry = e.nextElement();
			String name = entry.getName();
			// Log.i(TAG,"found file :" + name);
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
			if (name.startsWith("lib/"+sys+"/")) {
				if (entry.isDirectory()) {
					continue;
				}
				libEntries.add(entry);
			}
		}
		/*
		 * new EasyFor<List<ZipEntry>>(archLibEntries.values()) {
		 * 
		 * @Override public void onNewElement(List<ZipEntry> element) { new
		 * EasyFor<ZipEntry>(element){
		 * 
		 * @Override public void onNewElement(ZipEntry element) {
		 * Log.i(TAG,element.getName()); } }; } };
		 */

//		List<ZipEntry> libEntries = archLibEntries.get(ARCH.toLowerCase());
//		if (libEntries == null) {
//			libEntries = archLibEntries.get(DEF_ARCH_1);
//			if (libEntries == null) {
//				libEntries = archLibEntries.get(DEF_ARCH_2);
//				if (libEntries == null) {
//					libEntries = archLibEntries.get(DEF_ARCH_3);
//				}
//			}
//		}
		int libs = 0;// 是否包含so
		if (libEntries != null) {
			if (!to.exists()) {
				return libs;
			}
			for (ZipEntry libEntry : libEntries) {
				String name = libEntry.getName();
				String pureName = name.substring(name.lastIndexOf('/') + 1);
				File target = new File(to, pureName);
				libs++;
				if (target.exists()) {
					target.delete();
				}
			}
		}
		return libs;
	}

	private static boolean extractLibFile(ZipFile zip, File to) throws ZipException, IOException {

//		Map<String, List<ZipEntry>> archLibEntries = new HashMap<String, List<ZipEntry>>();
		String sys = ARCH.toLowerCase();
		if(sys.equals("i686")){
			sys = "x86";
		}else if(sys.equals(DEF_ARCH_2)){
			sys = DEF_ARCH_2;
		}else {
			sys = DEF_ARCH_1;
		}
		List<ZipEntry> libEntries = new ArrayList<ZipEntry>();
		for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			ZipEntry entry = e.nextElement();
			String name = entry.getName();
			// Log.i(TAG,"found file :" + name);
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
			if (name.startsWith("lib/"+sys+"/")) {
				if (entry.isDirectory()) {
					continue;
				}
				libEntries.add(entry);
			}
		}
		/*
		 * new EasyFor<List<ZipEntry>>(archLibEntries.values()) {
		 * 
		 * @Override public void onNewElement(List<ZipEntry> element) { new
		 * EasyFor<ZipEntry>(element){
		 * 
		 * @Override public void onNewElement(ZipEntry element) {
		 * Log.i(TAG,element.getName()); } }; } };
		 */

//		List<ZipEntry> libEntries = archLibEntries.get(ARCH.toLowerCase());
//		if (libEntries == null) {
//			libEntries = archLibEntries.get(DEF_ARCH_1);
//			if (libEntries == null) {
//				libEntries = archLibEntries.get(DEF_ARCH_2);
//				if (libEntries == null) {
//					libEntries = archLibEntries.get(DEF_ARCH_3);
//				}
//			}
//		}
		boolean hasLib = false;// 是否包含so
		if (libEntries != null) {
			hasLib = true;
			if (!to.exists()) {
				to.mkdirs();
			}
			for (ZipEntry libEntry : libEntries) {
				String name = libEntry.getName();
				String pureName = name.substring(name.lastIndexOf('/') + 1);
				File target = new File(to, pureName);
				writeToFile(zip.getInputStream(libEntry), target);
			}
		}

		return hasLib;
	}
    private static void writeToFile(InputStream dataIns, File target)
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
}
