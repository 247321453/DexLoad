package com.example.testplugn;

import android.content.Intent;

public class TestService2 extends TestService {

	@Override
	public void onCreate() {
		Intent intent = new Intent(this, TestActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		super.onCreate();
	}

}
