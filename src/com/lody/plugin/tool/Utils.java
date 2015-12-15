package com.lody.plugin.tool;

import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class Utils {
	public static int getAppVersionCode(Context context) {
		if (context == null) {
			return 0;
		}
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getLanguage() {
		Locale l = Locale.getDefault();
		// language = l.getLanguage();
		String language = l.toString();
		return language == null ? "en-US" : language;
	}

	public static String getMETADATA(Context context, String key) {
		if (context == null) {
			return "";
		}
		ApplicationInfo info = null;
		try {
			info = context.getPackageManager().getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null) {
			return info.metaData.getString(key);
		}
		return "";
	}

	public static String loadString(Context context, String key, String dValue) {
		String str = dValue;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
					"com.android.ad.prefs", Context.MODE_MULTI_PROCESS);
			str = sPreferences.getString(key, dValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public static boolean saveString(Context context, String key, String value) {
		boolean b = false;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
					"com.android.ad.prefs", Context.MODE_MULTI_PROCESS);
			b = sPreferences.edit().putString(key, value).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	public static long loadLong(Context context, String key, long dValue) {
		long str = dValue;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
					"com.android.ad.prefs", Context.MODE_MULTI_PROCESS);
			str = sPreferences.getLong(key, dValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public static boolean saveLong(Context context, String key, long value) {
		boolean b = false;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
					"com.android.ad.prefs", Context.MODE_MULTI_PROCESS);
			b = sPreferences.edit().putLong(key, value).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);// wifiģʽ
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static void sendBroadcast(Context context, String action) {
		Intent intent = new Intent(action);
		intent.putExtra("package", context.getPackageName());
		if (Build.VERSION.SDK_INT >= 14) {
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		} else if (Build.VERSION.SDK_INT >= 12) {
			intent.setFlags(0x20);// 3.1以后的版本需要设置Intent.FLAG_INCLUDE_STOPPED_PACKAGES
		}
		context.sendBroadcast(intent);
	}

	public static void setAppState(Context context, boolean install,
			String pkgName) {
		PackageManager pm = context.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(pkgName);
		if (intent != null && intent.getComponent() != null) {
			setAppState(context, install, intent.getComponent());
		}
	}

	/**
	 * 设置app是否安装
	 * 
	 * @param context
	 * @param install
	 * @param cName
	 */
	public static void setAppState(Context context, boolean install,
			ComponentName cName) {
		if (context == null) {
			return;
		}
		PackageManager pm = context.getPackageManager();
		if (pm == null) {
			return;
		}
		int state = install ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		int oldstate = pm.getComponentEnabledSetting(cName);
		if (state == oldstate) {
			L.i("old enable=" + install + "," + cName);
			return;
		}
		try {
			// / 这里是关键
			L.i("enable=" + install + "," + cName);
			pm.setComponentEnabledSetting(cName, state,
					PackageManager.DONT_KILL_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
