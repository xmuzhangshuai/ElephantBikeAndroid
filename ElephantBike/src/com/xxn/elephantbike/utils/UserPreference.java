package com.xxn.elephantbike.utils;

import java.util.Date;

import com.xxn.elephantbike.table.UserTable;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * 处理用户的信息，中间流程信息的保存操作
 * 
 * @ClassName: UserPreference
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年4月4日 下午5:52:58
 */
public class UserPreference {
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	public static final String USER_SHAREPREFERENCE = "userSharePreference";// 用户SharePreference
	private Context context;

	public UserPreference(Context context) {
		this.context = context;
		sp = context.getSharedPreferences(USER_SHAREPREFERENCE, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	// 打印用户信息
	public void printUserInfo() {
		LogTool.i("是否登录: " + getUserLogin());
		LogTool.i("手机验证码: " + getU_VERIFY_CODE());
		LogTool.i("登录手机号: " + getU_tel());
		LogTool.i("该账户是否完成支付: " + getU_IS_PAY());
		LogTool.i("该账户订单是否完成: " + getU_IS_FINISH());
		LogTool.i("该账户是否冻结: " + getU_IS_FROZEN());
		LogTool.i("创建时间: " + DateTimeTools.DateToString(getU_CreatTime()));
		LogTool.i("access_token: " + getAccess_token());
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

	/**
	 * 不保存电话号码
	 */
	public void clearthird() {
		editor.clear();
		editor.commit();
	}

	// 记录用户是否登录
	public boolean getUserLogin() {
		return sp.getBoolean("login", false);
	}

	public void setUserLogin(boolean login) {
		editor.putBoolean("login", login);
		editor.commit();
	}

	// 手机号
	public String getU_tel() {
		return sp.getString(UserTable.U_TEL, "");
	}

	public void setU_tel(String u_tel) {
		editor.putString(UserTable.U_TEL, u_tel);
		editor.commit();
	}

	// 微信订单号
	public String getWxOutTradeNo() {
		return sp.getString("wxOutTradeNo", "");
	}

	public void setWxOutTradeNo(String wxOutTradeNo) {
		editor.putString("wxOutTradeNo", wxOutTradeNo);
		editor.commit();
	}

	// 该用户是否为会员
	public String getU_is_vip() {
		return sp.getString(UserTable.U_IS_VIP, "");
	}

	public void setU_is_vip(String is_vip) {
		editor.putString(UserTable.U_IS_VIP, is_vip);
		editor.commit();
	}

	// 该用户的会员期限
	public String getU_VIP_DATE() {
		return sp.getString(UserTable.U_VIP_DATE, "");
	}

	public void setU_VIP_DATE(String vip_date) {
		editor.putString(UserTable.U_VIP_DATE, vip_date);
		editor.commit();
	}

	// 该用户是否有新消息
	public String getU_IS_MESSAGE() {
		return sp.getString(UserTable.U_IS_MESSAGE, "");
	}

	public void setU_IS_MESSAGE(String isMessage) {
		editor.putString(UserTable.U_IS_MESSAGE, isMessage);
		editor.commit();
	}

	// 验证码
	public String getU_VERIFY_CODE() {
		return sp.getString(UserTable.U_VERIFY_CODE, "");
	}

	public void setU_VERIFY_CODE(String code) {
		editor.putString(UserTable.U_VERIFY_CODE, code);
		editor.commit();
	}

	// 记录用户是否冻结
	public String getU_IS_FROZEN() {
		return sp.getString(UserTable.U_IS_FROZEN, "");
	}

	public void setU_IS_FROZEN(String isfrozen) {
		if (null == isfrozen) {
			editor.putString(UserTable.U_IS_FROZEN, "");
			editor.commit();
		} else {
			editor.putString(UserTable.U_IS_FROZEN, isfrozen);
			editor.commit();
		}
	}

	// 记录用户订单是否完成
	public String getU_IS_FINISH() {
		return sp.getString(UserTable.U_IS_FINISH, "");
	}

	public void setU_IS_FINISH(String isfinish) {
		editor.putString(UserTable.U_IS_FINISH, isfinish);
		editor.commit();
	}

	// 记录用户是否完成支付
	public String getU_IS_PAY() {
		return sp.getString(UserTable.U_IS_PAY, "");
	}

	public void setU_IS_PAY(String ispay) {
		editor.putString(UserTable.U_IS_PAY, ispay);
		editor.commit();
	}

	// access_token
	public String getAccess_token() {
		return sp.getString(UserTable.U_ACCESS_TOKEN, "");
	}

	public void setAccess_token(String access_token) {
		if (!TextUtils.isEmpty(access_token)) {
			editor.putString(UserTable.U_ACCESS_TOKEN, access_token);
			editor.commit();
		}
	}

	// 创建时间
	public Date getU_CreatTime() {
		Long time = sp.getLong(UserTable.U_CREATE_TIME, 0);
		if (time != 0) {
			return new Date(time);
		} else {
			return null;
		}
	}

	public void setU_CreatTime(Date creatTime) {
		if (creatTime != null) {
			editor.putLong(UserTable.U_CREATE_TIME, creatTime.getTime());
			editor.commit();
		}
	}

	// 身份证URL
	public String getU_ID_CARD() {
		return sp.getString(UserTable.ID_CARD_FRONT, "");
	}

	public void setU_ID_CARD(String u_id_card) {
		editor.putString(UserTable.ID_CARD_FRONT, u_id_card);
		editor.commit();
	}

	// 学生证URL
	public String getU_STU_CARD() {
		return sp.getString(UserTable.STU_CARD_FRONT, "");
	}

	public void setU_STU_CARD(String u_stu_card) {
		editor.putString(UserTable.STU_CARD_FRONT, u_stu_card);
		editor.commit();
	}

	// 学生的学号
	public String getU_CARD_NUM() {
		return sp.getString(UserTable.STU_CARD_NUM, "");
	}

	public void setU_CARD_NUM(String u_card_num) {
		editor.putString(UserTable.STU_CARD_NUM, u_card_num);
		editor.commit();
	}

	// 学生所属的大学
	public String getU_COLLEGE() {
		return sp.getString(UserTable.STU_COLLEGE, "");
	}

	public void setU_COLLEGE(String u_college) {
		editor.putString(UserTable.STU_COLLEGE, u_college);
		editor.commit();
	}

	// 学生的姓名
	public String getU_NAME() {
		return sp.getString(UserTable.STU_NAME, "");
	}

	public void setU_NAME(String u_name) {
		editor.putString(UserTable.STU_NAME, u_name);
		editor.commit();
	}

	// 用户所在的经度
	public String getLongitude() {
		return sp.getString(UserTable.LONGITUDE, "");
	}

	public void setLongitude(String longitude) {
		editor.putString(UserTable.LONGITUDE, longitude);
		editor.commit();
	}

	// 用户所在的纬度
	public String getLatitude() {
		return sp.getString(UserTable.LATITUDE, "");
	}

	public void setLatitude(String latitude) {
		editor.putString(UserTable.LATITUDE, latitude);
		editor.commit();
	}

}
