package com.xxn.elephantbike.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**   
*    
*/
public class DateTimeTools {
	@SuppressLint("SimpleDateFormat")
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
	static DateFormat formatter2 = SimpleDateFormat.getDateInstance();

	public DateTimeTools() {
		// TODO Auto-generated constructor stub

	}

	public static Date StringToDate(String dateString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 将日期格式转化为字符串返回
	 * @param date
	 * @return
	 */
	public static String DateToString(Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return format.format(date);
		} else {
			return "";
		}
	}

	public static String DateToStringWithYMD(Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			return format.format(date);
		} else {
			return "";
		}
	}

	public static String DateToStringForCN(Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("yy年MM月dd日   HH:mm");
			return format.format(date);
		} else {
			return "";
		}
	}

	public static String getMonAndDay(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		return format.format(date);
	}

	public static String getDateString(Date date) {
		return formatter2.format(date);
	}

	public static String getTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
		return format.format(new Date(time));
	}

	public static String getHourAndMin(long time) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(new Date(time));
	}

	public static String getHourAndMin(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(date);
	}

	public static String getChatTime(long timesamp) {
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		Date today = new Date(System.currentTimeMillis());
		Date otherDay = new Date(timesamp);
		int temp = Integer.parseInt(sdf.format(today)) - Integer.parseInt(sdf.format(otherDay));

		switch (temp) {
		case 0:
			result = "今天 " + getHourAndMin(timesamp);
			break;
		case 1:
			result = "昨天 " + getHourAndMin(timesamp);
			break;
		case 2:
			result = "前天 " + getHourAndMin(timesamp);
			break;

		default:
			result = getTime(timesamp);
			break;
		}

		return result;
	}

	/**
	 * 返回当前系统时间
	 * @return Date格式
	 */
	public static Date getCurrentDateTime() {
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return curDate;
	}

	/**
	 * 返回当前系统时间
	 * @return String格式
	 */
	public static String getCurrentDateTimeForString() {
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}

	/**
	 * 获取当前日期
	 * @return
	 */
	public static Date getCurrentDate() {
		Date date = new Date();
		return date;
	}

	public static String getCurrentDateForString() {
		return formatter2.format(getCurrentDate());
	}

	/**
	 * 比较和当前时间差，返回String
	 * @param date
	 * @return
	 */
	public static String getInterval(Date date) {
		String intervalString = "";
		// 获取时间差
		Map<String, Integer> interval = DateTimeTools.compareTo(DateTimeTools.getCurrentDateTime(), date);
		// 根据时间先后设置人性化时间提醒
		if (interval.get("year") >= 1) {
			intervalString = interval.get("year") + "年前";
		} else if (interval.get("month") >= 1) {
			intervalString = interval.get("month") + "月前";
		} else if (interval.get("day") >= 1) {
			intervalString = interval.get("day") + "天前";
		} else if (interval.get("hour") >= 1) {
			intervalString = interval.get("hour") + "小时前";
		} else if (interval.get("minute") >= 3) {
			intervalString = interval.get("minute") + "分钟前";
		} else {
			intervalString = "刚刚";
		}
		return intervalString;
	}

	/**
	 * 计算日期时间之间的差值， date1得时间必须大于date2的时间
	 * @param date1 
	 * @param date2
	 * @return {@link java.util.Map} Map的键分别为, day(天), hour(小时),minute(分钟)和second(秒)。
	 */
	public static Map<String, Long> timeDifference(final Date date1, final Date date2) {
		if (date1 == null || date2 == null) {
			throw new NullPointerException("date1 and date2 can't null");
		}
		long mim1 = date1.getTime();
		long mim2 = date2.getTime();
		if (mim1 < mim2) {
			throw new IllegalArgumentException(String.format("date1[%s] not be less than date2[%s].", mim1 + "", mim2 + ""));
		}
		long m = (mim1 - mim2 + 1) / 1000l;
		long mday = 24 * 3600;
		final Map<String, Long> map = new HashMap<String, Long>();
		map.put("day", m / mday);
		m = m % mday;
		map.put("hour", (m) / 3600);
		map.put("minute", (m % 3600) / 60);
		map.put("second", (m % 3600 % 60));
		return map;
	}

	public static Map<String, Integer> compareTo(final Date date1, final Date date2) {
		if (date1 == null || date2 == null) {
			return null;
		}
		long time1 = date1.getTime();
		long time2 = date2.getTime();
		long time = Math.max(time1, time2) - Math.min(time1, time2);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("year", (calendar.get(Calendar.YEAR) - 1970) > 0 ? (calendar.get(Calendar.YEAR) - 1970) : 0);
		map.put("month", (calendar.get(Calendar.MONTH) - 1) > 0 ? (calendar.get(Calendar.MONTH) - 1) : 0);
		map.put("day", (calendar.get(Calendar.DAY_OF_MONTH) - 1) > 0 ? (calendar.get(Calendar.DAY_OF_MONTH) - 1) : 0);
		map.put("hour", (calendar.get(Calendar.HOUR_OF_DAY) - 8) > 0 ? (calendar.get(Calendar.HOUR_OF_DAY) - 8) : 0);
		map.put("minute", calendar.get(Calendar.MINUTE) > 0 ? calendar.get(Calendar.MINUTE) : 0);
		map.put("second", calendar.get(Calendar.SECOND) > 0 ? calendar.get(Calendar.SECOND) : 0);
		return map;
	}
}
