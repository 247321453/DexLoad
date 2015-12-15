package com.lody.plugin;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.lody.plugin.bean.LAPK;
import com.lody.plugin.manager.LApkManager;
import com.lody.plugin.reflect.Reflect;

class Test {
	LAPK mAPK;
	Object Pluginer;

	public Test(Context context, String apkFile) {
		try {
			mAPK = new LAPK();
			// 设置路径
			mAPK.attach(apkFile);
			// class加载器
			mAPK.bindDexLoader(context);
			// 初始化资源
			LApkManager.initApk(mAPK, context);
		} catch (Exception e) {
			e.printStackTrace();
			mAPK = null;
		}
	}

	public boolean isLoad() {
		return mAPK != null;
	}

	public void text(Context context) {
		// 系统自带类或者本应用的类，构造对象
		// TextView tv=new android.widget.TextView(context);
		TextView tv = Reflect.on("android.widget.TextView").create(context)
				.get();
		// 调用方法tv.setText("hello");
		Reflect.on(tv).call("setText", "hello");
		// 设置(私有)变量boolean mFreezesText
		if ((Boolean) Reflect.on(tv).get("mFreezesText")) {
			Reflect.on(tv).set("mFreezesText", false);
		}
		// 静态方法TextUtils.isEmpty("a");
		Reflect.on("android.text.TextUtils").call("isEmpty", "a");
		// 外部apk的类，所有on方法，加一个参数DexClassLoader
		Reflect.on(mAPK.pluginLoader, "android.text.TextUtils");
		Bundle extras = new Bundle();
		// 启动某个activity
		LPluginOpener.startActivity(context, mAPK.pluginPath,
				"con.test.MainAcitivty", extras);
		// 启动某个service,服务只能存在一个
		LPluginOpener.startService(context, mAPK.pluginPath,
				"con.test.MainService", extras);
	}
}
