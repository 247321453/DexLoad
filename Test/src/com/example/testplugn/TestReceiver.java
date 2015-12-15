package com.example.testplugn;

import com.example.testplugn.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.e("onReceive:"+intent);
	}

}
