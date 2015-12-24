package com.lody.plugin.api;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.lody.plugin.BuildConfig;
import com.lody.plugin.bean.Reflect;

/**
 * Created by lody on 2015/3/27.
 *
 * @author Lody
 *
 *         负责转移插件的跳转目标<br>
 * @see android.app.Activity#startActivity(android.content.Intent)
 */
public class LPluginInstrument extends Instrumentation {

	Instrumentation pluginIn;
	String pluginPath;
	Reflect instrumentRef;

	public LPluginInstrument(Instrumentation pluginIn, String pluginPath) {
		this.pluginIn = pluginIn;
		instrumentRef = Reflect.on(pluginIn);
		this.pluginPath = pluginPath;
	}
	/**
	 * @Override
	 */
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options) {

		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return instrumentRef
					.call("execStartActivity", who, contextThread, token, target, intent, requestCode, options).get();
		}
		LProxyControl.setActivity(who, intent, componentName.getClassName(), pluginPath);
        if (BuildConfig.DEBUG)
            Log.i(LPluginConfig.TAG, "Jump to " + componentName + "[" + pluginPath + "]");
		return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode, options)
				.get();

	}

	/**
	 * @Override
	 */
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode) {

		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode)
					.get();
		}
		LProxyControl.setActivity(who, intent, componentName.getClassName(), pluginPath);
        if (BuildConfig.DEBUG)
            Log.i(LPluginConfig.TAG, "Jump to " + componentName + "[" + pluginPath + "]");
		return instrumentRef.call("execStartActivity", who, contextThread, token, target, intent, requestCode).get();

	}

	@Override
	public void onStart() {
		pluginIn.onStart();
	}

	@Override
	public void onCreate(Bundle arguments) {
		pluginIn.onCreate(arguments);
	}

	@Override
	public void onDestroy() {
		pluginIn.onDestroy();
	}

	@Override
	public boolean onException(Object obj, Throwable e) {
		return pluginIn.onException(obj, e);
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		pluginIn.callActivityOnCreate(activity, icicle);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		pluginIn.callActivityOnNewIntent(activity, intent);
	}

	@Override
	public void callApplicationOnCreate(Application app) {
		pluginIn.callApplicationOnCreate(app);
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		pluginIn.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnPause(Activity activity) {
		pluginIn.callActivityOnDestroy(activity);
	}

}
