package com.xxn.elephantbike.utils;

import com.xxn.elephantbike.table.OrderTable;
import com.xxn.elephantbike.table.ShareTable;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 与用户订单相关的本地文件操作
 * 
 * @ClassName: OrderPreference
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年4月6日 下午4:24:18
 */
public class OrderPreference {
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	public static final String ORDER_PREFERENCE = "OrderPreference";// 用户SharePreference
	private Context context;

	public OrderPreference(Context context, String file) {
		sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	public OrderPreference(Context context) {
		this.context = context;
		sp = context.getSharedPreferences(ORDER_PREFERENCE, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	/**
	 * 清空数据
	 * 
	 * @Title: clearAll
	 * @Description: TODO
	 * @return: void
	 */
	public void clearAll() {
		editor.clear();
		editor.commit();
	}

	// 获取单车号
	public String getBike_ID() {
		return sp.getString(OrderTable.TEMP_BIKE_ID, "");
	}

	public void setBike_ID(String bikeid) {
		editor.putString(OrderTable.TEMP_BIKE_ID, bikeid);
		editor.commit();
	}

	// 单车是否丢失
	public Boolean getBIKE_LOST_FLAG() {
		return sp.getBoolean(OrderTable.BIKE_LOST_FLAG, false);
	}

	public void setBIKE_LOST_FLAG(Boolean isbikelost) {
		editor.putBoolean(OrderTable.BIKE_LOST_FLAG, isbikelost);
		editor.commit();
	}

	// 打印出该preference中的信息
	public void printUserInfo() {

	}

}
