package com.example.testplugn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
	//	int i = PluginManager.onCreateService(this);
//		LogUtil.e("onCreate:" + this.getClass().getName()+", all:"+i);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

}
