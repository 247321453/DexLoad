package com.example.testplugn;

import android.content.Intent;

public class TestService3 extends TestService {

	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, TestService4.class));
	}

}
