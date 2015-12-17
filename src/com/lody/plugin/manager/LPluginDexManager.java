package com.lody.plugin.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.plugin.LPluginConfig;
import com.lody.plugin.reflect.Reflect;
import com.lody.plugin.tool.LLogUtil;
import com.lody.plugin.tool.NativeLibUnpacker;

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
			if (LPluginConfig.MULIT_DEX) {
				parent = cxt.getClassLoader().getParent();
			} else {
				parent = getSystemLoader(cxt);
			}
			final String dexOutputPath = getPluginDir(cxt);
			final String libOutputPath = getPluginlibDir(cxt);

			pluginDexLoader = new LPluginDexManager(dexPath, dexOutputPath, libOutputPath, parent);
			if (LPluginConfig.MULIT_DEX) {
				try {
					Reflect.on(cxt.getClassLoader()).set("parent", pluginDexLoader);
				} catch (Exception e) {
					LLogUtil.e(e.getMessage());
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
				LLogUtil.d("getSystemClassLoader");
			}
		} else {
			LLogUtil.d("currentActivityThread.getSystemContext");
		}
		return parent;
	}
}
