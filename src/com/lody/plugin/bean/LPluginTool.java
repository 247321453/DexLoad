package com.lody.plugin.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by lody on 2015/3/24.
 */
public class LPluginTool {
    static final String PREFS_FILE = "com.android.dexplugin.prefs";

    @SuppressWarnings("deprecation")
    private static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sPreferences;
        if (Build.VERSION.SDK_INT > 19) {
            sPreferences = context.getSharedPreferences(
                    PREFS_FILE, Context.MODE_PRIVATE);
        } else {
            sPreferences = context.getSharedPreferences(
                    PREFS_FILE, Context.MODE_MULTI_PROCESS);
        }
        return sPreferences;
    }


    public  static String loadString(Context context, String key, String dValue) {
        String str = dValue;
        try {
            SharedPreferences sPreferences = getSharedPreferences(context);
            str = sPreferences.getString(key, dValue);
        } catch (Exception e) {
        }
        return str;
    }

    public static boolean saveString(Context context, String key, String value) {
        boolean b = false;
        try {
            SharedPreferences sPreferences = getSharedPreferences(context);
            b = sPreferences.edit().putString(key, value).commit();
        } catch (Exception e) {
        }
        return b;
    }
	/**
	 * 获取一个apk的信息
	 *
	 * @param cxt
	 *            应用上下文
	 * @param apkPath
	 *            apk所在绝对路径
	 * @return
	 */
	public static PackageInfo getAppInfo(Context cxt, String apkPath)
			throws PackageManager.NameNotFoundException {
		PackageManager pm = cxt.getPackageManager();
		PackageInfo pkgInfo = null;
		pkgInfo = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
		return pkgInfo;
	}

	/**
	 * 获取指定APK应用名
	 *
	 * @param cxt
	 *            应用上下文
	 * @param apkPath
	 *            apk所在绝对路径
	 * @return
	 */
    public  static CharSequence getAppName(Context cxt, String apkPath)
			throws PackageManager.NameNotFoundException {
		PackageManager pm = cxt.getPackageManager();
		PackageInfo pkgInfo = getAppInfo(cxt, apkPath);
		if (pkgInfo == null) {
			return null;
		} else {
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			if (Build.VERSION.SDK_INT >= 8) {
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;
			}
			return pm.getApplicationLabel(appInfo);
		}
	}
}
