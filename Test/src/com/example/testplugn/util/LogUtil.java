package com.example.testplugn.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

/**
 * App的测试配置项
 *
 *
 */
public class LogUtil {

	/**
	 * debug模式，发布打包需要置为false，可以通过混淆让调试的log文本从代码文件中消除，避免被反编译时漏泄相关信息。
	 */
	public static boolean IS_DEBUG = true;
	private static File logFile;
	private static final String TAG= "TestService";
	private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

	public static void logMethodName(Object object) {
		if (IS_DEBUG) {
			try {
				Log.v(getLogTag(object), getMethodName());
				writeFile(getMethodName());
			} catch (Throwable e) {
				if (IS_DEBUG) {
				}
			}
		}
	}
	public static void v(String msg) {
		if (IS_DEBUG) {
			Log.v(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (IS_DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (IS_DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (IS_DEBUG) {
			Log.w(TAG, msg);
			writeFile(TAG + ":" + msg);
		}
	}

	public static void e(String msg) {
		if (IS_DEBUG) {
			Log.e(TAG, msg);
			writeFile(TAG + ":" + msg);
		}
	}
	public static void v(String tag, String msg) {
		if (IS_DEBUG) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (IS_DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (IS_DEBUG) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (IS_DEBUG) {
			Log.w(tag, msg);
			writeFile(tag + ":" + msg);
		}
	}

	public static void e(String tag, String msg) {
		if (IS_DEBUG) {
			Log.e(tag, msg);
			writeFile(tag + ":" + msg);
		}
	}

	public static void logMethodName(Class<?> cls) {
		if (IS_DEBUG) {
			try {
				Log.v(getLogTag(cls), getMethodName());
				writeFile(getMethodName());
			} catch (Throwable e) {
				if (IS_DEBUG) {
				}
			}
		}
	}

	private static String getLogTag(Object object) {
		return object.getClass().getSimpleName() + "[" + object.hashCode() + "]";
	}

	private static String getMethodName() {
		final Thread current = Thread.currentThread();
		final StackTraceElement trace = current.getStackTrace()[4];
		return trace.getMethodName();
	}

	public static void logParams(String tag, Object... params) {
		if (IS_DEBUG) {
			for (Object obj : params) {
				Log.i(tag, "" + obj);
				writeFile("" + obj);
			}
		}
	}

	public static void logNetworkRequest(Object object, String request, String response) {
		if (IS_DEBUG) {
			Log.i(getLogTag(object), String.format("【Request】:%s", request));
			writeFile(String.format("【Request】:%s", request));
			Log.i(getLogTag(object), String.format("【Response】:%s", response));
			writeFile(String.format("【Response】:%s", response));
		}
	}

	public static void logFields(Class<?> classType) {
		if (IS_DEBUG) {
			try {
				final String name = classType.getSimpleName();
				final Field[] fs = classType.getDeclaredFields();
				for (Field f : fs) {
					Log.i(name, "Filed:" + f.getName());
					writeFile("Filed:" + f.getName());
				}
			} catch (Exception e) {
				if (IS_DEBUG) {
				}
			}
		}
	}

	public static void logMethodWithParams(Object object, Object... params) {
		if (IS_DEBUG) {
			try {
				final StringBuilder sb = new StringBuilder();
				sb.append(getMethodName()).append(":");
				for (Object obj : params) {
					sb.append('[').append(obj).append("], ");
				}
				Log.v(getLogTag(object), sb.toString());
				writeFile(sb.toString());
			} catch (Exception e) {
				if (IS_DEBUG) {
				}
			}
		}
	}

	public static void logMethodWithException(Object object, Exception paramException) {
		if (IS_DEBUG) {
			try {
				StringWriter localStringWriter = new StringWriter();
				final StringBuilder sb = new StringBuilder();
				sb.append(getMethodName()).append(":");
				paramException.printStackTrace(new PrintWriter(localStringWriter));
				sb.append('[').append(localStringWriter.toString()).append("], ");
				Log.e(getLogTag(object), sb.toString());
				writeFile(sb.toString());
				return;
			} catch (Exception localException) {
				localException.toString();
			}
		}
	}

	public static void logMemoryInfo() {
		if (IS_DEBUG) {
			try {
				// final ActivityManager activityManager = (ActivityManager)
				// getActivity().getSystemService(Context
				// .ACTIVITY_SERVICE);
				// activityManager.getMemoryClass();
				final String tag = "MM_INFO";
				// Log.i(tag, "Class " + activityManager.getMemoryClass());
				final long mb = 1024 * 1024l;
				// Get VM Heap Size by calling:
				Log.i(tag, "VM Heap Size:" + Runtime.getRuntime().totalMemory() / mb);

				// Get VM Heap Size Limit by calling:
				Log.i(tag, "VM Heap Size Limit:" + Runtime.getRuntime().maxMemory() / mb);

				// Get Allocated VM Memory by calling:
				Log.i(tag, "Allocated VM Memory:"
						+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
						/ mb);

				// Get Native Allocated Memory by calling:
				Log.i(tag, "Native Allocated Memory:" + Debug.getNativeHeapAllocatedSize() / mb);

			} catch (Exception e) {
				if (IS_DEBUG) {
				}
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	public static synchronized void writeFile(String logText) {
		if (!IS_DEBUG) {
			deleteLogFile();
			return;
		}

		if (Environment.getExternalStorageState().equals("mounted")) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());
			String strDateTime = dateFormat.format(curDate);
			String strLog = strDateTime + " " + logText;
			try {
				File logFilePath = new File(sdPath + "/IFunBow/log");
				if (!logFilePath.exists()) {
					logFilePath.mkdirs();
				}

				logFile = new File(sdPath + "/IFunBow/log" + File.separator + "ifunbow_log.txt");
				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				FileOutputStream outputStream = new FileOutputStream(logFile, true);
				outputStream.write(strLog.getBytes());
				outputStream.write("\n".getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean deleteLogFile() {
		return new File(sdPath + "/IFunBow/log" + File.separator + "ifunbow_log.txt").delete();
	}

}
