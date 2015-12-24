package com.lody.plugin.app;

import android.content.ComponentName;
import android.content.Intent;

import com.lody.plugin.LPluginOpener;

public final class LProxyServiceMirror extends LProxyService {

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
        Class<?> freeservice = LPluginOpener.getService(this, sSERVICE_APK_PATH, serviceName, true);
        if (freeservice == null) {
            freeservice = LProxyService.class;
        }
        service.setClass(this, freeservice);
        super.stopService(service);
        LPluginOpener.setPluginService(this, freeservice, sSERVICE_APK_PATH, serviceName);
        return super.startService(service);
    }
}
