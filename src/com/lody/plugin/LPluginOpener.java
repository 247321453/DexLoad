package com.lody.plugin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.lody.plugin.api.LPluginConfig;
import com.lody.plugin.api.LProxyControl;
import com.lody.plugin.app.LProxyService;
import com.lody.plugin.app.LProxyServiceMirror;
import com.lody.plugin.bean.LPluginTool;

/**
 * Created by lody on 2015/3/24. Created by lody on 2015/3/24.
 */
public class LPluginOpener {
    /**
     * 是否使用插件的标题
     */
    public static boolean usePluginTitle = true;

    /** 合并classloader，但是apk的类是不能重复 */
    public static boolean MULIT_DEX = false;

    /**
     * 直接启动一个apk
     *
     * @param context    当前上下文
     * @param pluginPath 插件路径
     */
    public static void startPlugin(Context context, String pluginPath) {
        startPlugin(context, pluginPath, null);
    }

    /**
     * 直接启动一个apk
     *
     * @param context    当前上下文
     * @param pluginPath 插件路径
     * @param args       携带数据
     */
    public static void startPlugin(Context context, String pluginPath, Bundle args) {
        startActivity(context, pluginPath, null, null);
    }

    /**
     * 启动插件中的指定activity
     *
     * @param context
     * @param pluginPath
     * @param activityName 要启动的插件的activity名
     */
    public static void startActivity(Context context, String pluginPath, String activityName) {
        startActivity(context, pluginPath, activityName, null);
    }

    /**
     * 启动插件中的指定activity
     *
     * @param context
     * @param pluginPath
     * @param activityName 要启动的插件的activity名
     * @param args         携带数据
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
     * @param serviceName 要启动的插件的service名
     */
    public static void startService(Context context, String pluginPath, String serviceName) {
        startService(context, pluginPath, serviceName, null);
    }

    /**
     * 启动插件中的指定service
     *
     * @param context
     * @param pluginPath
     * @param serviceName 要启动的插件的service名
     * @param args        携带数据
     */
    public static void startService(Context context, String pluginPath, String serviceName, Bundle args) {
        Class<?> service = getService(context, pluginPath, serviceName, true);
        if (service == null) {
            service = LProxyService.class;
        }
        startService(context, pluginPath, serviceName, args, service);
    }

    /***
     * @param context
     * @param pluginPath
     * @param serviceName  要启动的插件的service名
     * @param args         携带数据
     * @param proxyService 代理服务名
     */
    public static void startService(Context context, String pluginPath, String serviceName, Bundle args,
                                    Class<?> proxyService) {
        if (pluginPath == null || serviceName == null) {
            return;
        }
        // 服务名
        String _class = getPluginService(context, proxyService);
        // apk
        String _apk = getPluginPath(context, proxyService);
        if (pluginPath.equalsIgnoreCase(_apk) && serviceName.equalsIgnoreCase(_class)) {
            if (BuildConfig.DEBUG)
                Log.d(LPluginConfig.TAG, "service is running...");
            try {
                context.startService(new Intent(context, proxyService));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (BuildConfig.DEBUG)
                Log.d(LPluginConfig.TAG, "service be start...");
            Intent i = new Intent(context, proxyService);
            if (args != null) {
                i.putExtras(args);
            }
            // 停止服务
            context.stopService(i);
            // 保存加载的内容
            setPluginService(context, proxyService, pluginPath, serviceName);
            context.startService(i);
        }
    }

    public static Class<?> getService(Context context, String apkPath, String serviceClass, boolean onlyexist) {
        return getService(context, apkPath, serviceClass, onlyexist,
                new Class[]{LProxyService.class, LProxyServiceMirror.class});
    }

    /***
     * @param context
     * @param apkPath
     * @param serviceClass
     * @param onlyexist    仅查找已经启动的服务
     * @param services
     * @return
     */
    public static Class<?> getService(Context context, String apkPath, String serviceClass, boolean onlyexist, Class<?>[] services) {
        if (apkPath == null || serviceClass == null) {
            return null;
        }
        for (Class<?> service : services) {
            // 服务名
            String _class = getPluginService(context, service);
            // apk
            String _apk = getPluginPath(context, service);
            if (apkPath.equalsIgnoreCase(_apk) && serviceClass.equalsIgnoreCase(_class)) {
                // 包一致,类一致
                return service;
            }
            if (!onlyexist) {
                if (TextUtils.isEmpty(_class) || Service.class.getName().equalsIgnoreCase(_class)) {
                    // 服务名为空
                    return service;
                }
            }
        }
        return null;
    }

    public static void setPluginService(Context context, Class<?> _class, String apkPath, String serviceName) {
        LPluginTool.saveString(context, _class.getSimpleName() + ".SERVICE_APK_PATH", apkPath);
        LPluginTool.saveString(context, _class.getSimpleName() + ".SERVICE_CLASS_NAME", serviceName);
    }

    public static String getPluginPath(Context context, Class<?> _class) {
        return LPluginTool.loadString(context, _class.getSimpleName() + ".SERVICE_APK_PATH", null);
    }

    public static String getPluginService(Context context, Class<?> _class) {
        return LPluginTool.loadString(context, _class.getSimpleName() + ".SERVICE_CLASS_NAME", null);
    }
}
