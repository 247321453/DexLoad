package com.lody.plugin.manager;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lody.plugin.bean.LAPK;
import com.lody.plugin.reflect.Reflect;
import com.lody.plugin.tool.L;
import com.lody.plugin.tool.LPluginTool;
import com.lody.plugin.tool.NativeLibUnpacker;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

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
			L.i("Not found Plugin on :" +  apkPath);
			return false;
		}
		L.i("Init a plugin on" + apkPath);
		try{
		if (!apk.canUse()) {
			L.i("Plugin is not been init,init it now！");
			fillPluginInfo(apk, ctx);
			fillPluginRes(apk, ctx);
			fillPluginApplication(apk, ctx);
		} else {
			L.i("Plugin have been init.");
		}
		}catch(Exception e){
			L.e(e.getMessage());
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
			L.e("PluginNotExistException:"+apk.pluginPath);
		}
		if (info == null) {
			L.e("Can't create Plugin from :"+apk.pluginPath);
		}
		apk.setPluginPkgInfo(info);
		apk.setApplicationName(info.applicationInfo.className);
	}

	private static void fillPluginRes(LAPK apk, Context ctx) {
		try {
			AssetManager assetManager = AssetManager.class.newInstance();
			Reflect assetRef = Reflect.on(assetManager);
			assetRef.call("addAssetPath", apk.pluginPath);
			L.i("Assets = " + assetManager);
			apk.setPluginAssets(assetManager);

			Resources pluginRes = new Resources(assetManager, ctx.getResources().getDisplayMetrics(),
					ctx.getResources().getConfiguration());
			L.i("Res = " + pluginRes);
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
			L.e("PluginCreateFailedException:Not found ClassLoader in plugin!");
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
