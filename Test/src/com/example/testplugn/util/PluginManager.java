package com.example.testplugn.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lody.plugin.LPluginOpener;
import com.lody.plugin.manager.LApkManager;
import com.lody.plugin.service.LProxyService;
import com.lody.plugin.service.LProxyService1;
import com.lody.plugin.service.LProxyService2;
import com.lody.plugin.service.LProxyService3;
import com.lody.plugin.service.LProxyServiceMirror;
import com.lody.plugin.tool.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

public class PluginManager {
	public static final String SERVICE_NAME = "ConfigService";
	public static final String META_NAME = "PluginService";
	static final Class<?>[] ALLSERVERS = new Class<?>[] { LProxyServiceMirror.class, LProxyService.class,
			LProxyService1.class, LProxyService2.class, LProxyService3.class };
	static final Class<?>[] SERVERS = new Class<?>[] { LProxyService1.class, LProxyService2.class,
			LProxyService3.class };

	public static List<String> getServices(Context context, String file) {
		List<String> list = new ArrayList<String>();
		final PackageInfo info = context.getPackageManager().getPackageArchiveInfo(file,
				PackageManager.GET_META_DATA | PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS);
		if (info != null) {
			if (info.services != null) {
				for (ServiceInfo sinfo : info.services) {
					String name = sinfo.name;
					if (name.endsWith("." + SERVICE_NAME)) {
						list.add(sinfo.name);
					}
				}
			}
			if (info.applicationInfo != null && info.applicationInfo.metaData != null) {
				String services = info.applicationInfo.metaData.getString(META_NAME);
				if (!TextUtils.isEmpty(services)) {
					String[] sinfos = services.split(";");
					for (String s : sinfos) {
						if (!TextUtils.isEmpty(s)) {
							list.add(s);
						}
					}
				}
			}
		}
		return list;
	}

	public static String getPluginFile(Context context, String file) {
		String md5 = MD5Utils.getInstance().getFileHash(file);
		File f = new File(context.getFilesDir(), md5 + ".jar");
		return f.getAbsolutePath();
	}

	public static boolean isPlugn(Context context, String file) {
		return getServices(context, file).size() == 0;
	}

	/**
	 * 启动插件服务
	 * 
	 * @param context
	 */
	public static int onCreateService(Context context) {
		int i = 0;
		for (Class<?> service : SERVERS) {
			String _class = LProxyService.getPluginService(context, service);
			if (!TextUtils.isEmpty(_class)) {
				context.startService(new Intent(context, service));
				i++;
			}
		}
		return i;
	}

	/**
	 * 
	 * @param context
	 * @param file
	 * @return false则是满了，或者找不到服务名
	 */
	public static boolean addPlugn(Context context, String file, String serviceName) {
		if (TextUtils.isEmpty(serviceName)) {
			return false;
		}
		for (Class<?> service : SERVERS) {
			String _class = LProxyService.getPluginService(context, service);
			if (TextUtils.isEmpty(_class)) {
				// 复制到data目录
				String dataFile = getPluginFile(context, file);
				File f = new File(dataFile);
				if (!f.exists()) {
					FileUtils.copyFile(new File(file), new File(dataFile));
				} else {
					LogUtil.e("插件已经存在:" + dataFile);
				}
				LogUtil.i("开始加载服务:" + service);
				try {
					LPluginOpener.startService(context, dataFile, serviceName, null, service);
				} catch (Exception e) {

				}
				return true;
			} else {
				if (TextUtils.equals(serviceName, _class)) {
					// 已经加载了，先判断md5值，再启动一次服务
					String dataFile = getPluginFile(context, file);
					String nowFile = LProxyService.getPluginPath(context, service);
					if (!TextUtils.equals(dataFile, nowFile)) {
						continue;
					} else {
						// md5值一致
						LogUtil.i("TestService", "插件已经加载，启动服务:" + service);
						try {
							context.startService(new Intent(context, service));
						} catch (Exception e) {

						}
					}
					return true;
				}
			}
		}
		LogUtil.i("TestService", "没有空位置加载服务");
		// 已经满了
		return false;
	}

	/***
	 * 卸载插件
	 * 
	 * @param context
	 * @param file
	 * @param serviceName
	 * @return
	 */
	public static boolean removePlugn(Context context, String file) {
		if (TextUtils.isEmpty(file)) {
			return false;
		}
		String dataFile = getPluginFile(context, file);
		LogUtil.i("开始卸载插件");
		for (Class<?> service : ALLSERVERS) {
			String apk = LProxyService.getPluginPath(context, service);
			if (TextUtils.equals(apk, dataFile)) {
				LogUtil.i("停止服务:" + service.getName());
				context.stopService(new Intent(context, service));
				LProxyService.setPluginService(context, service, null, null);
			}
		}
		File f = new File(dataFile);
		if (f.exists()) {
			LogUtil.i("删除插件");
			LApkManager.remove(context, dataFile);
			f.delete();
		}
		LogUtil.i("卸载插件完成");
		return true;
	}
}
