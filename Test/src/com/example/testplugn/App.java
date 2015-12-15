package com.example.testplugn;

import com.example.testplugn.util.LogUtil;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		LogUtil.e("App.onCreate");
		super.onCreate();
	}
}
