package com.xxn.elephantbike.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xxn.elephantbike.R;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CommonTools {

	/**
	* 判断程序是否在运行
	* @param context
	* @return
	*/
	//	public static boolean isAppRunning(Context context) {
	//		boolean isAppRunning = false;
	//		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	//		List<RunningTaskInfo> list = am.getRunningTasks(100);
	//		for (RunningTaskInfo info : list) {
	//			if (info.topActivity.getPackageName().equals(Constants.PACKAGENAME)
	//					&& info.baseActivity.getPackageName().equals(Constants.PACKAGENAME)) {
	//				isAppRunning = true;
	//				//find it, break 
	//				break;
	//			}
	//		}
	//		return isAppRunning;
	//	}

	/**
	 * 获取android当前可用内存大小
	 * @param context
	 * @return
	 */
	public static String getAvailMemory(Context context) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		//mi.availMem; 当前系统的可用内存 

		return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化 
	}

	/**
	 * 获取总内存大小
	 * @return
	 */
	public static String getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";// 系统内存信息文件 
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小 

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				LogTool.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte 
			localBufferedReader.close();

		} catch (IOException e) {
		}
		return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化 
	}

	/**
	 * 检测Sdcard是否存在
	 * 
	 * @return
	 */
	public static boolean isExitsSdcard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}

	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName();
		else
			return "";
	}

	/**
	 * 短暂显示Toast消息
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShortToast(Context context, String message) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_toast, null);
		TextView text = (TextView) view.findViewById(R.id.toast_message);
		text.setText(message);
		Toast toast = new Toast(context);
		toast.setDuration(2000);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 300);
		toast.setView(view);
		toast.show();
	}
	
	/**
	 * 长时间来显示Toast消息
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLongToast(Context context, String message) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_toast, null);
		TextView text = (TextView) view.findViewById(R.id.toast_message);
		text.setText(message);
		Toast toast = new Toast(context);
		toast.setDuration(2000);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.BOTTOM, 0, 300);
		toast.setView(view);
		toast.show();
	}

	/**
	* 判断学号格式
	*/
	public static boolean isStudentNO(String stuNumber) {
		Pattern pattern = Pattern.compile("^[0-9]{6,18}$");
		Matcher matcher = pattern.matcher(stuNumber);
		return matcher.matches();
	}

	/**
	* 判断手机号码
	*/
	public static boolean isMobileNO(String mobiles) {
		if (mobiles == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9])|(17[0-9]))\\d{8}$");
		Matcher matcher = pattern.matcher(mobiles);
		return matcher.matches();
	}

	/**
	 * 判断邀请码格式
	 * @return
	 */
	public static boolean isInviteCode(String code) {
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z]{6}$");
		Matcher matcher = pattern.matcher(code);
		return matcher.matches();
	}

	/**
	 * 验证密码是否符合要求
	 * 以字母开头，长度在6~18之间，只能包含字符、数字和下划线
	 * @return
	 */
	public static boolean isPassValid(String pass) {
		Pattern pattern = Pattern.compile("^[0-9a-zA-Z]{6,18}$");
		Matcher matcher = pattern.matcher(pass);
		return matcher.matches();
	}

}
