package com.lody.plugin.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import com.lody.plugin.BuildConfig;
import com.lody.plugin.api.LPluginConfig;
import com.lody.plugin.LPluginOpener;
import com.lody.plugin.bean.LPluginBug;
import com.lody.plugin.bean.ILoadPlugin;
import com.lody.plugin.bean.LActivityPlugin;
import com.lody.plugin.bean.LPluginTool;
import com.lody.plugin.api.PluginActivityCallback;
import com.lody.plugin.api.PluginActivityControl;
import com.lody.plugin.api.LApkManager;
import com.lody.plugin.api.LCallbackManager;
import com.lody.plugin.api.LPluginBugManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * Created by lody on 2015/3/27.
 */
public class LActivityProxy extends Activity implements ILoadPlugin {

    protected LActivityPlugin remotePlugin;
    protected String pluginActivityName;
    protected String pluginDexPath;

    @Override
    public LActivityPlugin loadPlugin(Activity ctx, String apkPath) {
        // 插件必须要确认有没有经过初始化，不然只是空壳
        remotePlugin = new LActivityPlugin(ctx, apkPath);
        return remotePlugin;

    }

    @Override
    public LActivityPlugin loadPlugin(Activity ctx, String apkPath, String activityName) {
        LActivityPlugin plugin = loadPlugin(ctx, apkPath);
        plugin.setTopActivityName(activityName);
        fillPlugin(plugin);
        return plugin;
    }

    /**
     * 装载插件
     *
     * @param plugin
     */
    @Override
    public void fillPlugin(LActivityPlugin plugin) {
        if (plugin == null) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "PluginNotExistException:Plugin is null!");
            return;
        }
        String apkPath = plugin.getPluginPath();
        File apk = new File(apkPath);
        if (!apk.exists()) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG, "NotFoundPluginException" + apkPath);
            //不存在dex
            return;
        }
        if (!this.remotePlugin.from().canUse()) {
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Plugin is not been init,init it now！");
            LApkManager.initApk(plugin.from(), this);
            // remotePlugin.from().debug();
        } else {
            if (BuildConfig.DEBUG)
                Log.i(LPluginConfig.TAG, "Plugin have been init.");
        }
        fillPluginTheme(plugin);
        fillPluginActivity(plugin);
    }

    private void fillPluginTheme(LActivityPlugin plugin) {

        Theme pluginTheme = plugin.from().pluginRes.newTheme();
        pluginTheme.setTo(super.getTheme());
        plugin.setTheme(pluginTheme);

        PackageInfo packageInfo = plugin.from().pluginPkgInfo;
        String mClass = plugin.getTopActivityName();

        if (BuildConfig.DEBUG)
            Log.i(LPluginConfig.TAG, "Before fill Plugin 's Theme,We check the plugin:info = " + packageInfo
                    + "topActivityName = " + mClass);

        int defaultTheme = packageInfo.applicationInfo.theme;
        ActivityInfo curActivityInfo = null;
        for (ActivityInfo a : packageInfo.activities) {
            if (a.name.equals(mClass)) {
                curActivityInfo = a;
                if (a.theme != 0) {
                    defaultTheme = a.theme;
                    if (BuildConfig.DEBUG)
                        Log.i(LPluginConfig.TAG, "Find theme=" + defaultTheme);
                } else if (defaultTheme != 0) {
                    // ignore
                    if (BuildConfig.DEBUG)
                        Log.i(LPluginConfig.TAG, "Find theme : ignore");
                } else {
                    // 支持不同系统的默认Theme
                    if (Build.VERSION.SDK_INT >= 14) {
                        defaultTheme = android.R.style.Theme_DeviceDefault;
                    } else {
                        defaultTheme = android.R.style.Theme;
                    }
                    if (BuildConfig.DEBUG)
                        Log.i(LPluginConfig.TAG, "Find theme : defaultTheme");
                }
                break;
            }
        }

        pluginTheme.applyStyle(defaultTheme, true);

        setTheme(defaultTheme);
        if (curActivityInfo != null) {
            getWindow().setSoftInputMode(curActivityInfo.softInputMode);
        }

        if (LPluginOpener.usePluginTitle) {
            CharSequence title = null;
            try {
                title = LPluginTool.getAppName(this, plugin.getPluginPath());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (title != null)
                setTitle(title);
        }

    }

    /**
     * 装载插件的Activity
     *
     * @param plugin
     */
    private void fillPluginActivity(LActivityPlugin plugin) {
        try {
            String top = plugin.getTopActivityName();
            if (top == null) {
                top = plugin.from().pluginPkgInfo.activities[0].name;
                plugin.setTopActivityName(top);
            }
            Activity myPlugin = (Activity) plugin.from().pluginLoader
                    .loadClass(plugin.getTopActivityName())
                    .newInstance();
            plugin.setCurrentPluginActivity(myPlugin);

        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,"" + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable ex) {

                LPluginBug bug = new LPluginBug();
                bug.error = ex;
                bug.errorTime = System.currentTimeMillis();
                bug.errorThread = thread;
                bug.errorPlugin = remotePlugin;
                bug.processId = android.os.Process.myPid();
                LPluginBugManager.callAllBugListener(bug);

            }
        });
        super.onCreate(savedInstanceState);
        final Bundle pluginMessage = getIntent().getExtras();

        if (pluginMessage != null) {
            pluginActivityName = pluginMessage.getString(LPluginConfig.KEY_PLUGIN_ACT_NAME,
                    LPluginConfig.DEF_PLUGIN_CLASS_NAME);
            pluginDexPath = pluginMessage.getString(LPluginConfig.KEY_PLUGIN_DEX_PATH,
                    LPluginConfig.DEF_PLUGIN_DEX_PATH);
        } else {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,"PluginCreateFailedException:Please put the Plugin Path!");
            finish();
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG,"pluginActivityName=" + pluginActivityName);
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG, "pluginDexPath=" + pluginDexPath);
        if (pluginDexPath.equals(LPluginConfig.DEF_PLUGIN_DEX_PATH)) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,"PluginCreateFailedException:Please put the Plugin Path!");
            finish();
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG, "loadPlugin");
        remotePlugin = loadPlugin(LActivityProxy.this, pluginDexPath);

        if (!pluginActivityName.equals(LPluginConfig.DEF_PLUGIN_CLASS_NAME)) {
            remotePlugin.setTopActivityName(pluginActivityName);
        }
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG, "fillPlugin");
        try {
            fillPlugin(remotePlugin);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }
        // remotePlugin.from().debug();
        if (this.getClass().getName().equals(pluginActivityName)) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,"pluginActivityName==this");
            finish();
            return;
        }
        PluginActivityControl control = new PluginActivityControl(pluginDexPath,
                LActivityProxy.this,
                remotePlugin.getCurrentPluginActivity(), remotePlugin.from().pluginApplication);
        remotePlugin.setControl(control);
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG,"dispatchProxyToPlugin");
        if (!control.dispatchProxyToPlugin()) {
            if (BuildConfig.DEBUG)
                Log.e(LPluginConfig.TAG,"dispatchProxyToPlugin:fail");
            finish();
            return;
        }
        if (BuildConfig.DEBUG)
            Log.d(LPluginConfig.TAG,"pluginActivity.onCreate");
        try {
            control.callOnCreate(savedInstanceState);
            LCallbackManager.callAllOnCreate(savedInstanceState);
        } catch (Exception e) {
            processError(e);
        }
    }

    private void processError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public Resources getResources() {
        if (remotePlugin == null)
            return super.getResources();
        return remotePlugin.from().pluginRes == null ? super.getResources()
                : remotePlugin.from().pluginRes;
    }

    @Override
    public Theme getTheme() {
        if (remotePlugin == null)
            return super.getTheme();
        return remotePlugin.getTheme() == null ? super.getTheme() : remotePlugin.getTheme();
    }

    @Override
    public AssetManager getAssets() {
        if (remotePlugin == null)
            return super.getAssets();
        return remotePlugin.from().pluginAssets == null ? super.getAssets()
                : remotePlugin.from().pluginAssets;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (remotePlugin == null) {
            return super.getClassLoader();
        }
        if (remotePlugin.from().canUse()) {
            return remotePlugin.from().pluginLoader;
        }
        return super.getClassLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callOnResume();
            LCallbackManager.callAllOnResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (remotePlugin == null) {
            return;
        }

        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {

            try {
                caller.callOnStart();
                LCallbackManager.callAllOnStart();
            } catch (Exception e) {

                processError(e);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {

            {
                try {
                    caller.callOnDestroy();
                    LCallbackManager.callAllOnDestroy();
                } catch (Exception e) {
                    processError(e);
                }
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {

            try {
                caller.callOnPause();
                LCallbackManager.callAllOnPause();
            } catch (Exception e) {
                processError(e);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (remotePlugin == null) {
            super.onBackPressed();
        }

        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            try {
                caller.callOnBackPressed();
                LCallbackManager.callAllOnBackPressed();
            } catch (Exception e) {
                processError(e);
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {

            {
                try {
                    caller.callOnStop();
                    LCallbackManager.callAllOnStop();
                } catch (Exception e) {
                    processError(e);
                }

            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (remotePlugin == null) {
            return;
        }

        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            try {
                caller.callOnRestart();
                LCallbackManager.callAllOnRestart();
            } catch (Exception e) {
                processError(e);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (remotePlugin == null) {
            return super.onKeyDown(keyCode, event);
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {

            LCallbackManager.callAllOnKeyDown(keyCode, event);
            return caller.callOnKeyDown(keyCode, event);

        }

        return super.onKeyDown(keyCode, event);
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
        Class<?> freeservice = LPluginOpener.getService(this, pluginDexPath, serviceName, true);
        if (freeservice != null) {
            service.setClass(this, freeservice);
            super.stopService(service);
            LPluginOpener.setPluginService(this, freeservice, pluginDexPath, serviceName);
        } else {
            service.setClass(this, LProxyService.class);
            // 停止服务
            super.stopService(service);
            LPluginOpener.setPluginService(this, LProxyService.class, pluginDexPath, serviceName);
        }
        return super.startService(service);
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callDump(prefix, fd, writer, args);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callOnConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callOnPostResume();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callOnDetachedFromWindow();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (remotePlugin == null) {
            return super.onCreateView(name, context, attrs);
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            return caller.callOnCreateView(name, context, attrs);
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (remotePlugin == null) {
            return super.onCreateView(parent, name, context, attrs);
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            return caller.callOnCreateView(parent, name, context, attrs);
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (remotePlugin == null) {
            return;
        }
        PluginActivityCallback caller = remotePlugin.getControl();
        if (caller != null) {
            caller.callOnNewIntent(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getControl().getPluginRef().call("onActivityResult", requestCode, resultCode,
                data);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {

        super.onAttachFragment(fragment);
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getCurrentPluginActivity().onAttachFragment(fragment);
    }

    @Override
    public View onCreatePanelView(int featureId) {

        if (remotePlugin == null)
            return super.onCreatePanelView(featureId);
        return remotePlugin.getCurrentPluginActivity().onCreatePanelView(featureId);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

        super.onOptionsMenuClosed(menu);
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getCurrentPluginActivity().onOptionsMenuClosed(menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {

        super.onPanelClosed(featureId, menu);
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getCurrentPluginActivity().onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (remotePlugin == null) {
            return super.onKeyUp(keyCode, event);
        }
        return remotePlugin.getCurrentPluginActivity().onKeyUp(keyCode, event);
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getCurrentPluginActivity().onAttachedToWindow();
    }

    @Override
    public CharSequence onCreateDescription() {

        if (remotePlugin == null)
            return super.onCreateDescription();

        return remotePlugin.getCurrentPluginActivity().onCreateDescription();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (remotePlugin == null)
            return super.onGenericMotionEvent(event);

        return remotePlugin.getCurrentPluginActivity().onGenericMotionEvent(event);
    }

    @Override
    public void onContentChanged() {

        super.onContentChanged();
        if (remotePlugin == null) {
            return;
        }
        remotePlugin.getCurrentPluginActivity().onContentChanged();
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {

        if (remotePlugin == null) {
            return super.onCreateThumbnail(outBitmap, canvas);
        }
        return remotePlugin.getCurrentPluginActivity().onCreateThumbnail(outBitmap, canvas);
    }

}
