package com.lody.plugin.tool;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

public class TimerUtil {
	public static void cancel(Context context, PendingIntent pintent) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(pintent);
	}

	/**
	 * 延时启动
	 * @param context
	 * @param interval 过多少时间启动
	 * @param pintent
	 * @param needWakeup
	 */
	public static void setAlarm(Context context, long interval, PendingIntent pintent,
			boolean needWakeup) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(pintent);
		long time = System.currentTimeMillis();
		if (needWakeup) {
			mgr.set(AlarmManager.RTC_WAKEUP, time + interval, pintent);// 即使屏幕关闭一样会启动
		} else {
			mgr.set(AlarmManager.RTC, time + interval, pintent);// 即使屏幕关闭一样会启动
		}
	}

	/**
	 * 定时启动
	 * @param context
	 * @param hour  0-24
	 * @param minute
	 * @param second
	 * @param pintent
	 * @param needWakeup
	 */
	public static void setAlarm(Context context, int hour, int minute, int second,
			PendingIntent pintent, boolean needWakeup) {
		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(pintent);
		long time = getTime(System.currentTimeMillis(), hour, minute, second);
		if (needWakeup) {
			mgr.set(AlarmManager.RTC_WAKEUP, time, pintent);// 即使屏幕关闭一样会启动
		} else {
			mgr.set(AlarmManager.RTC, time, pintent);// 即使屏幕关闭一样会启动
		}
	}

	public static long getTime(long millis, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		long time = calendar.getTimeInMillis();
		if (time < System.currentTimeMillis()) {
			// 时间小则是第二天
			calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
		}
		return calendar.getTimeInMillis();
	}
}
