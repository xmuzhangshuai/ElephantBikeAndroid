package com.xxn.elephantbike.utils;

import com.xxn.elephantbike.table.ShareTable;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 */
public class SharePreference {
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	public static final String SHARE_PREFERENCE = "SharePreference";// 用户SharePreference
	private Context context;

	public SharePreference(Context context, String file) {
		sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	public SharePreference(Context context) {
		this.context = context;
		sp = context.getSharedPreferences(SHARE_PREFERENCE, Context.MODE_PRIVATE);
		editor = sp.edit();
	}
	
	/**
	 * 清空数据
	 * @Title: clearAll 
	 * @Description: TODO
	 * @return: void
	 */
	public void clearAll() {
		editor.clear();
		editor.commit();
	}

	// 获得活动是否需要再显示
	public String getACTIVITY_isdisplay() {
		//默认值为取到1为显示状态
		return sp.getString(ShareTable.ACTIVITY_ISDISPLAY, "1");
	}

	public void setACTIVITY_isdisplay(String isdisplay_status) {
		editor.putString(ShareTable.ACTIVITY_ISDISPLAY, isdisplay_status);
		editor.commit();
	}

	// 获得活动状态
	public String getActivity_status() {
		return sp.getString(ShareTable.ACTIVITY_STATUS, "");
	}

	public void setActivity_status(String status) {
		editor.putString(ShareTable.ACTIVITY_STATUS, status);
		editor.commit();
	}

	// 获得图片URL
	public String getImage_url() {
		return sp.getString(ShareTable.IMAGE_URL, "");
	}

	public void setImage_url(String imageurl) {
		editor.putString(ShareTable.IMAGE_URL, imageurl);
		editor.commit();
	}

	// 获得链接URL
	public String getLink_url() {
		return sp.getString(ShareTable.LINK_URL, "");
	}

	public void setLink_url(String linkurl) {
		editor.putString(ShareTable.LINK_URL, linkurl);
		editor.commit();
	}

	// 获得活动状态
	public String getActivity_status_1() {
		return sp.getString(ShareTable.ACTIVITY_STATUS_1, "");
	}

	public void setActivity_status_1(String status) {
		editor.putString(ShareTable.ACTIVITY_STATUS_1, status);
		editor.commit();
	}

	// 获得图片URL
	public String getImage_url_1() {
		return sp.getString(ShareTable.IMAGE_URL_1, "");
	}

	public void setImage_url_1(String imageurl) {
		editor.putString(ShareTable.IMAGE_URL_1, imageurl);
		editor.commit();
	}

	// 获得链接URL
	public String getLink_url_1() {
		return sp.getString(ShareTable.LINK_URL_1, "");
	}

	public void setLink_url_1(String linkurl) {
		editor.putString(ShareTable.LINK_URL_1, linkurl);
		editor.commit();
	}

	// 获得活动状态
	public String getActivity_status_2() {
		return sp.getString(ShareTable.ACTIVITY_STATUS_2, "");
	}

	public void setActivity_status_2(String status) {
		editor.putString(ShareTable.ACTIVITY_STATUS_2, status);
		editor.commit();
	}

	// 获得图片URL
	public String getImage_url_2() {
		return sp.getString(ShareTable.IMAGE_URL_2, "");
	}

	public void setImage_url_2(String imageurl) {
		editor.putString(ShareTable.IMAGE_URL_2, imageurl);
		editor.commit();
	}

	// 获得链接URL
	public String getLink_url_2() {
		return sp.getString(ShareTable.LINK_URL_2, "");
	}

	public void setLink_url_2(String linkurl) {
		editor.putString(ShareTable.LINK_URL_2, linkurl);
		editor.commit();
	}

	// 打印出该preference中的信息
	public void printUserInfo() {

	}

}
