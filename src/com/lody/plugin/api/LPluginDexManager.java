package com.lody.plugin.api;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.lody.plugin.BuildConfig;
import com.lody.plugin.LPluginOpener;
import com.lody.plugin.bean.Reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

/**
 * Created by lody on 2015/3/24. 插件的核心加载器<br>
 * 已支持Native
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class LPluginDexManager extends DexClassLoader {

	private static final Map<String, LPluginDexManager> pluginLoader = new ConcurrentHashMap<String, LPluginDexManager>();
	// public static String finalApkPath;

	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	protected LPluginDexManager(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		// finalApkPath = dexPath;
		NativeLibUnpacker.unPackSOFromApk(dexPath, libraryPath);
	}

	public static String getPluginlibDir(Context context) {
		return context.getDir("plugin_lib", Context.MODE_PRIVATE).getAbsolutePath();
	}

	public static String getPluginDir(Context context) {
		return context.getDir("plugin", Context.MODE_PRIVATE).getAbsolutePath();
	}

	/**
	 * 返回apk对应的加载器，不会重复加载同样的资源
	 */
	public static LPluginDexManager getClassLoader(String dexPath, Context cxt) {
		LPluginDexManager pluginDexLoader = pluginLoader.get(dexPath);
		if (pluginDexLoader == null) {
			// 获取到app的启动路径
			ClassLoader parent = null;
			if (LPluginOpener.MULIT_DEX) {
				parent = cxt.getClassLoader().getParent();
			} else {
				parent = getSystemLoader(cxt);
			}
			final String dexOutputPath = getPluginDir(cxt);
			final String libOutputPath = getPluginlibDir(cxt);

			pluginDexLoader = new LPluginDexManager(dexPath, dexOutputPath, libOutputPath, parent);
			if (LPluginOpener.MULIT_DEX) {
				try {
					Reflect.on(cxt.getClassLoader()).set("parent", pluginDexLoader);
				} catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        Log.i(LPluginConfig.TAG,""+e);
                }
			}
			pluginLoader.put(dexPath, pluginDexLoader);
		}
		return pluginDexLoader;
	}

	public static ClassLoader getSystemLoader(Context cxt) {
		ClassLoader parent = null;
		try {
			Context context = Reflect.on("android.app.ActivityThread").call("currentActivityThread")
					.call("getSystemContext").get();
			if (context != null) {
				parent = context.getClassLoader();
			}
		} catch (Exception e) {

		}
		if (parent == null) {
			parent = getSystemClassLoader();
			if (parent == null) {
				parent = cxt.getClassLoader();
			} else {
                if (BuildConfig.DEBUG)
                    Log.d(LPluginConfig.TAG, "getSystemClassLoader");
			}
		} else {
            if (BuildConfig.DEBUG)
                Log.d(LPluginConfig.TAG, "currentActivityThread.getSystemContext");
		}
		return parent;
	}
}
