package com.lody.plugin.api;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.lody.plugin.BuildConfig;
import com.lody.plugin.bean.LAPK;
import com.lody.plugin.bean.Reflect;
import com.lody.plugin.bean.LPluginTool;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lody on 2015/4/6.
 */
public final class LApkManager {

	private static final Map<String, LAPK> apks = new ConcurrentHashMap<String, LAPK>();

	public static LAPK get(String apkPath) {
		LAPK apk;
		apk = apks.get(apkPath);
		if (apk == null) {
			apk = new LAPK();
			apk.attach(apkPath);
			apks.put(apkPath, apk);
		}
		return apk;
	}

	public static void remove(Context context, String apkfile) {
		try {
			File file = new File(apkfile);
			String name = file.getName();
			int i = name.lastIndexOf(".");
			if (i > 0) {
				name = name.substring(0, i);
			}
			File dex = new File(LPluginDexManager.getPluginDir(context), name+".dex");
			if(dex.exists()){
				dex.delete();
			}
			NativeLibUnpacker.unPackSOFromApk(apkfile, LPluginDexManager.getPluginlibDir(context));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean initApk(LAPK apk, Context ctx) {
		String apkPath = apk.pluginPath;
		File file = new File(apkPath);
		if (!file.exists()){
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Not found Plugin on :" + apkPath);
			return false;
		}
        if (BuildConfig.DEBUG)
            Log.i(LPluginConfig.TAG, "Init a plugin on" + apkPath);
		try{
		if (!apk.canUse()) {
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Plugin is not been init,init it now！");
			fillPluginInfo(apk, ctx);
			fillPluginRes(apk, ctx);
			fillPluginApplication(apk, ctx);
		} else {
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Plugin have been init.");
		}
		}catch(Exception e){
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "" + e);
            return false;
		}
		return true;
	}

	private static void fillPluginInfo(LAPK apk, Context ctx) {
		if(apk==null){
			return;
		}
		PackageInfo info = null;
		try {
			info = LPluginTool.getAppInfo(ctx, apk.pluginPath);
		} catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "PluginNotExistException:" + apk.pluginPath);
		}
		if (info == null) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "Can't create Plugin from :" + apk.pluginPath);
		}
		apk.setPluginPkgInfo(info);
		apk.setApplicationName(info.applicationInfo.className);
	}

	private static void fillPluginRes(LAPK apk, Context ctx) {
		try {
			AssetManager assetManager = AssetManager.class.newInstance();
			Reflect assetRef = Reflect.on(assetManager);
			assetRef.call("addAssetPath", apk.pluginPath);
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Assets = " + assetManager);
			apk.setPluginAssets(assetManager);

			Resources pluginRes = new Resources(assetManager, ctx.getResources().getDisplayMetrics(),
					ctx.getResources().getConfiguration());
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Res = " + pluginRes);
			apk.setPluginRes(pluginRes);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fillPluginApplication(LAPK apk, Context ctx) {
		String applicationName = apk.applicationName;
		if (applicationName == null)
			return;
		if (applicationName.isEmpty())
			return;

		ClassLoader loader = apk.pluginLoader;
		if (loader == null){
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "PluginCreateFailedException:Not found ClassLoader in plugin!");
			return;
		}
		try {
			Application pluginApp = (Application) loader.loadClass(applicationName).newInstance();
			Reflect.on(pluginApp).call("attachBaseContext", ctx.getApplicationContext());
			apk.pluginApplication = pluginApp;
			pluginApp.onCreate();

		} catch (InstantiationException e) {
			// throw new PluginCreateFailedException(e.getMessage());
		} catch (IllegalAccessException e) {
			// throw new PluginCreateFailedException(e.getMessage());
		} catch (ClassNotFoundException e) {
			// throw new PluginCreateFailedException(e.getMessage());
		}
	}

}
