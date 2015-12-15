package com.lody.plugin;

import com.lody.plugin.tool.L;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class LProxyControl {

	public static void setActivity(Context context, Intent intent, String activity, String dexPath) {
		L.d("this=" + context.getClass());
		ComponentName componentName = intent.getComponent();
		if(Intent.ACTION_VIEW.equals(intent.getAction())){
			L.i("no set:ACTION_VIEW");
			return;
		}
		if (componentName != null && (LActivityProxy.class.getName().equals(componentName.getClassName())
				|| LActivityProxyMirror.class.getName().equals(componentName.getClassName()))) {
			// 已经设置了
		} else {
			String proxy = context.getClass().getName();
			if (LActivityProxy.class.getName().equals(proxy)) {
				proxy = LActivityProxyMirror.class.getName();
			} else if (LActivityProxyMirror.class.getName().equals(proxy)) {
				proxy = LActivityProxy.class.getName();
			} else {
				proxy = LActivityProxy.class.getName();
			}
			L.i("set LActivityProxy");
			intent.setClassName(context.getPackageName(), proxy);
			intent.putExtra(LPluginConfig.KEY_PLUGIN_DEX_PATH, dexPath);
			intent.putExtra(LPluginConfig.KEY_PLUGIN_ACT_NAME, activity);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

}
