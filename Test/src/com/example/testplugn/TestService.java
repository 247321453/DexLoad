package com.example.testplugn;

import com.example.testplugn.util.LogUtil;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class TestService extends Service {
	Service mService;
	@SuppressLint("HandlerLeak")
	Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				LogUtil.d("handleMessage by "+mService.getClass().getSimpleName());
				mHandler.removeMessages(0);
				mHandler.sendEmptyMessageDelayed(0, 5000);
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mService = this;
		LogUtil.d("onCreate by "+mService.getClass().getSimpleName());
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.d("onStartCommand:"+intent+" by "+mService.getClass().getSimpleName());
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 5000);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mHandler.removeMessages(0);
		LogUtil.d("onDestroy by "+mService.getClass().getSimpleName());
		stopSelf();
		super.onDestroy();
	}

}
