package com.lody.plugin.tool;

import android.content.Context;
import android.content.SharedPreferences;

/***
 * @hide
 */
public class LPrefUtils {
    static final String PREFS_FILE = "com.android.dexplugin.prefs";
	public static String loadString(Context context, String key, String dValue) {
		String str = dValue;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
                    PREFS_FILE, Context.MODE_MULTI_PROCESS);
			str = sPreferences.getString(key, dValue);
		} catch (Exception e) {
		}
		return str;
	}

	public static boolean saveString(Context context, String key, String value) {
		boolean b = false;
		try {
			SharedPreferences sPreferences = context.getSharedPreferences(
                    PREFS_FILE, Context.MODE_MULTI_PROCESS);
			b = sPreferences.edit().putString(key, value).commit();
		} catch (Exception e) {
		}
		return b;
	}
}
