package com.lody.plugin;

import com.lody.plugin.service.LProxyService;
import com.lody.plugin.tool.LLogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by lody on 2015/3/24. Created by lody on 2015/3/24.
 */
public class LPluginOpener {
	/**
	 * 直接启动一个apk
	 *
	 * @param context
	 *            当前上下文
	 * @param pluginPath
	 *            插件路径
	 */
	public static void startPlugin(Context context, String pluginPath) {
		startPlugin(context, pluginPath, null);
	}

	/**
	 * 直接启动一个apk
	 *
	 * @param context
	 *            当前上下文
	 * @param pluginPath
	 *            插件路径
	 * @param args
	 *            携带数据
	 */
	public static void startPlugin(Context context, String pluginPath, Bundle args) {
		startActivity(context, pluginPath, null, null);
	}

	/**
	 * 启动插件中的指定activity
	 * 
	 * @param context
	 * @param pluginPath
	 * @param activityName
	 *            要启动的插件的activity名
	 */
	public static void startActivity(Context context, String pluginPath, String activityName) {
		startActivity(context, pluginPath, activityName, null);
	}

	/**
	 * 启动插件中的指定activity
	 * 
	 * @param context
	 * @param pluginPath
	 * @param activityName
	 *            要启动的插件的activity名
	 * @param args
	 *            携带数据
	 */
	public static void startActivity(Context context, String pluginPath, String activityName, Bundle args) {
		Intent i = new Intent();
		if (args != null) {
			i.putExtras(args);
		}
		LProxyControl.setActivity(context, i, activityName, pluginPath);
		context.startActivity(i);
	}

	/**
	 * 启动插件中的指定service
	 * 
	 * @param context
	 * @param pluginPath
	 * @param serviceName
	 *            要启动的插件的service名
	 */
	public static void startService(Context context, String pluginPath, String serviceName) {
		startService(context, pluginPath, serviceName, null);
	}

	/**
	 * 启动插件中的指定service
	 * 
	 * @param context
	 * @param pluginPath
	 * @param serviceName
	 *            要启动的插件的service名
	 * @param args
	 *            携带数据
	 */
	public static void startService(Context context, String pluginPath, String serviceName, Bundle args) {
		Class<?> service = LProxyService.getService(context, pluginPath, serviceName, true);
		if (service == null) {
			service = LProxyService.class;
		}
		startService(context, pluginPath, serviceName, args, service);
	}

	/***
	 * 
	 * @param context
	 * @param pluginPath
	 * @param serviceName
	 *            要启动的插件的service名
	 * @param args
	 *            携带数据
	 * @param proxyService
	 *            代理服务名
	 */
	public static void startService(Context context, String pluginPath, String serviceName, Bundle args,
			Class<?> proxyService) {
		if (pluginPath == null || serviceName == null) {
			return;
		}
		// 服务名
		String _class = LProxyService.getPluginService(context, proxyService);
		// apk
		String _apk = LProxyService.getPluginPath(context, proxyService);
		if (pluginPath.equalsIgnoreCase(_apk) && serviceName.equalsIgnoreCase(_class)) {
			LLogUtil.d("service is running...");
			try {
				context.startService(new Intent(context, proxyService));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LLogUtil.d("service be start...");
			Intent i = new Intent(context, proxyService);
			if (args != null) {
				i.putExtras(args);
			}
			// 停止服务
			context.stopService(i);
			// 保存加载的内容
			LProxyService.setPluginService(context, proxyService, pluginPath, serviceName);
			context.startService(i);
		}
	}
}
