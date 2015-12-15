package com.lody.plugin.tool;

import com.lody.plugin.LPluginConfig;

import android.util.Log;

public class L {
	private static final String TAG= "LAPK";
	private static final boolean IS_DEBUG = LPluginConfig.IS_DEBUG;
	public static void v(String msg) {
		if (IS_DEBUG) {
			Log.v(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (IS_DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (IS_DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (IS_DEBUG) {
			Log.w(TAG, msg);
		}
	}

	public static void e(String msg) {
		if (IS_DEBUG) {
			Log.e(TAG, msg);
		}
	}
}
