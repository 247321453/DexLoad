package com.example.testplugn;

import com.example.testplugn.util.FileUtils;
import com.example.testplugn.util.PluginManager;
import com.kk.testplugn.R;
import com.lody.plugin.LPluginOpener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	static final String APK = "/sdcard/TestPlugn.apk";
	static final String APK2 = "/sdcard/TestPlugn2.apk";
	static final String APK3 = "/sdcard/admobi.apk";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FileUtils.CopyFromAsset(this, "TestPlugn.apk", APK);
		FileUtils.CopyFromAsset(this, "TestPlugn2.apk", APK2);
		FileUtils.CopyFromAsset(this, "admobi.apk", APK3);
		startService(new Intent(this, MainService.class));
		findViewById(R.id.btn01).setOnClickListener(this);
		findViewById(R.id.btn02).setOnClickListener(this);
		findViewById(R.id.btn03).setOnClickListener(this);
		findViewById(R.id.btn04).setOnClickListener(this);
		findViewById(R.id.btn05).setOnClickListener(this);
		findViewById(R.id.btn06).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn01:
			PluginManager.addPlugn(this, APK, TestService.class.getName());
			break;
		case R.id.btn02:
			PluginManager.addPlugn(this, APK, TestService2.class.getName());
			break;
		case R.id.btn03:
			PluginManager.addPlugn(this, APK, TestService3.class.getName());
			break;
		case R.id.btn04:
			PluginManager.addPlugn(this, APK2, TestService.class.getName());
			break;
		case R.id.btn05:
			PluginManager.removePlugn(this, APK);
			break;
		case R.id.btn06:
			// ClassLoader dexLoader = new DexClassLoader(APK3,
			// getCacheDir().getAbsolutePath(),
			// null, ClassLoader.getSystemClassLoader());
			// // ClassLoader dexLoader = getClassLoader();
			// try {
			// Class<?> serviceClass =
			// dexLoader.loadClass("com.lody.plugin.DaemonService");
			// Intent intent = new Intent(this, serviceClass);
			// startService(intent);
			// } catch (ClassNotFoundException e) {
			// e.printStackTrace();
			// }
			// LPluginOpener.startActivity(this, APK3,
			// "com.google.android.gms.ads.AdActivity");
			LPluginOpener.startService(this, APK3, "com.lody.plugin.DaemonService");
			break;
		default:
			break;
		}
	}
}
