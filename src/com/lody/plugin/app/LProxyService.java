package com.lody.plugin.app;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.text.TextUtils;

import com.lody.plugin.api.LApkManager;
import com.lody.plugin.api.LProxyControl;
import com.lody.plugin.LPluginOpener;
import com.lody.plugin.bean.LServicePlugin;
import com.lody.plugin.bean.Reflect;

public class LProxyService extends Service {

    protected LServicePlugin remote;
    protected boolean isInit = false;
    protected String sSERVICE_CLASS_NAME = Service.class.getName();
    protected String sSERVICE_APK_PATH = null;// LPluginDexManager.finalApkPath;

    @Override
    public IBinder onBind(Intent i) {
        if (isInit) {
            return remote.getCurrentPluginService().onBind(i);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if (intent != null) {
        // String serviceName = intent.getStringExtra("SERVICE_CLASS_NAME");
        // String apkPath = intent.getStringExtra("SERVICE_APK_PATH");
        // if (TextUtils.equals(apkPath, sSERVICE_APK_PATH)) {
        // // apk一致
        // if (TextUtils.equals(serviceName, sSERVICE_CLASS_NAME)) {
        // // 服务名一致
        // } else {
        // // 切换服务
        // isInit = fillService(apkPath, serviceName);
        // if (isInit) {
        // remote.getCurrentPluginService().onCreate();
        // }
        // }
        // } else {
        // isInit = fillService(apkPath, serviceName);
        // if (isInit) {
        // remote.getCurrentPluginService().onCreate();
        // }
        // }
        // }
        if (isInit) {
            return remote.getCurrentPluginService().onStartCommand(intent, flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String apkPath = LPluginOpener.getPluginPath(this, this.getClass());
        String serviceName = LPluginOpener.getPluginService(this, this.getClass());
        if (TextUtils.isEmpty(apkPath)) {
            apkPath = sSERVICE_APK_PATH;
        }
        if (TextUtils.isEmpty(serviceName)) {
            serviceName = sSERVICE_CLASS_NAME;
        }
        // 插件不存在
        isInit = fillService(apkPath, serviceName);
        if (isInit) {
            remote.getCurrentPluginService().onCreate();
        } else {
            LPluginOpener.setPluginService(this, this.getClass(), null, null);
            stopSelf();
        }
    }

    protected boolean fillService(String apkPath, String serviceName) {
        if (TextUtils.isEmpty(apkPath)) {
            return false;
        }
        sSERVICE_APK_PATH = apkPath;
        sSERVICE_CLASS_NAME = serviceName;
        LPluginOpener.setPluginService(this, this.getClass(), apkPath, serviceName);
        try {
            remote = new LServicePlugin(this, apkPath);
            remote.setTopServiceName(serviceName);
            remote.from().debug();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (!remote.from().canUse()) {
            try {
                LApkManager.initApk(remote.from(), this);
            } catch (Exception e) {
                return false;
            }
        }

        try {
            Service plugin = (Service) remote.from().pluginLoader.loadClass(remote.getTopServiceName()).newInstance();
            remote.setCurrentPluginService(plugin);
            Reflect thiz = Reflect.on(this);
            Reflect.on(plugin).call("attach", this, thiz.get("mThread"), getClass().getName(), thiz.get("mToken"),
                    getApplication(), thiz.get("mActivityManager"));
            return true;
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (isInit)
            remote.getCurrentPluginService().onStart(intent, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isInit)
            remote.getCurrentPluginService().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (isInit)
            return remote.getCurrentPluginService().onUnbind(intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isInit)
            remote.getCurrentPluginService().onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (isInit)
            remote.getCurrentPluginService().onRebind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (isInit)
            remote.getCurrentPluginService().onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (isInit)
            remote.getCurrentPluginService().onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (isInit)
            remote.getCurrentPluginService().onTaskRemoved(rootIntent);
    }

    @Override
    public Resources getResources() {
        if (isInit) {
            if (remote.from() == null)
                return super.getResources();
            return remote.from().pluginRes == null ?
                    super.getResources() : remote.from().pluginRes;
        }
        return super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (isInit) {
            if (remote.from() == null)
                return super.getAssets();
            return remote.from().pluginAssets == null ?
                    super.getAssets() : remote.from().pluginAssets;
        }
        return super.getAssets();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (isInit) {
            if (remote.from().canUse()) {
                return remote.from().pluginLoader;
            }
        }
        return super.getClassLoader();
    }

    @Override
    public void startActivity(Intent intent) {
        String activityName = intent.getComponent().getClassName();
        try {
            Class.forName(activityName);
            //存在这个类
            super.startActivity(intent);
            return;
        } catch (Exception e) {
            //不存在这个类
        }
        LProxyControl.setActivity(this, intent, activityName, sSERVICE_APK_PATH);
        super.startActivity(intent);
    }

    @Override
    public ComponentName startService(Intent service) {
        // 获取服务名
        String serviceName = service.getComponent().getClassName();
        try {
            Class.forName(serviceName);
            //存在这个类
            return super.startService(service);
        } catch (Exception e) {
            //不存在这个类
        }
        // 设置代理服务
        Class<?> freeservice = LProxyServiceMirror.class;
        service.setClass(this, freeservice);
        super.stopService(service);
        LPluginOpener.setPluginService(this, freeservice, sSERVICE_APK_PATH, serviceName);
        return super.startService(service);
    }
}
