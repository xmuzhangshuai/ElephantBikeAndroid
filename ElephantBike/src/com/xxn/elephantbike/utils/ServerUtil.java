package com.xxn.elephantbike.utils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.table.UserTable;
import com.xxn.elephantbike.ui.AccountAndPersonActivity;
import com.xxn.elephantbike.ui.AccountSubmitActivity;
import com.xxn.elephantbike.ui.LoginOrRegisterActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 *
 */
public class ServerUtil {
	private static ServerUtil instance;
	private UserPreference userPreference;

	public ServerUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 得到初始化了UserPreference的实例
	 * 
	 * @param context
	 * @return
	 */
	public static ServerUtil getInstance() {
		if (instance == null) {
			instance = new ServerUtil();
			instance.userPreference = BaseApplication.getInstance().getUserPreference();
		}
		return instance;
	}

	/**
	 * 如果用户已经登录过则直接进入扫描状态，并向后台发送登录请求,同时获取是否有公告的信息
	 */
	public <T> void login(final Context context, final Class<T> cls) {
		// 电话号码为空时仍需要登录
		if (userPreference.getU_tel().isEmpty()) {
			// 仍需重新登录
			Intent intent = new Intent(context, LoginOrRegisterActivity.class);
			context.startActivity(intent);
			// ((Activity)
			// context).overridePendingTransition(R.anim.push_left_in,
			// R.anim.push_left_out);
			((Activity) context).finish();
		} else {
			// 电话号码不为空时，此时需要请求服务器登录
			reLogin(userPreference.getU_tel() + "", context, cls);
		}
	}

	// 获取某个用户信息
	public <T> void reLogin(String phone, final Context context, final Class<T> cls) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put(UserTable.U_TEL, phone);
		// 已经登录了不需要验证码
		params.put(UserTable.U_VERIFY_CODE, "");
		// 是否已经登录（0为未登录，1为已经登录过）
		params.put(UserTable.U_IS_LOGIN, 1);

		// 向服务器发送请求再次登录操作
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				// 保存访问令牌
				jsonTool.saveAccess_token();
				if (status.equals("success")) {
					//1、清除掉本地的用户信息
					clearUserInfo();
					//2、 将服务器传回来的数据进行处理
					saveUser(jsonObject);
					// 此时获取到的userPreference就是最新的了，对用户的支付状态做判断
					if ("1".equals(userPreference.getU_IS_FINISH())) {
						// 2、未完成，跳转计费界面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "GuideActivity"); // 这里用来传值
						intent.setClass(context, AccountAndPersonActivity.class);
						context.startActivity(intent);
						((Activity) context).finish();
					}
					else if ("1".equals(userPreference.getU_IS_PAY())) {
						// 1、未支付，跳转至支付界面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "GuideActivity"); // 这里用来传值
						intent.setClass(context, AccountSubmitActivity.class);
						context.startActivity(intent);
						((Activity) context).finish();
					}
					else{
						// 3、直接跳转到扫描界面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "GuideActivity"); // 这里用来传值
						intent.setClass(context, cls);
						context.startActivity(intent);
						((Activity) context).finish();
					}
				} else {
					LogTool.e("status" + status);
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(context, "api/user/login", params, responseHandler);
	}
	
	/**
	 * 清除用户信息
	 * @Title: clearUserInfo 
	 * @Description: TODO
	 * @return: void
	 */
	private void clearUserInfo(){
		//是否完成支付
//		userPreference.setU_IS_PAY("");
//		//用户状态是否冻结
//		userPreference.setU_IS_FROZEN("");
//		//订单是否完成
//		userPreference.setU_IS_FINISH("");
	}

	/**
	 * 存储自己的信息
	 * @param jsonUserObject
	 */
	private void saveUser(final JSONObject jsonUserObject) {
		// TODO Auto-generated method stub
		try {
			// String userInfo = jsonUserObject.getString("user_info");
			// JSONObject userInfoJsonObject = new JSONObject(userInfo);

			String isfrozen = jsonUserObject.getString(UserTable.U_IS_FROZEN);
			//用户状态是否冻结
			userPreference.setU_IS_FROZEN(isfrozen);
			String isfinish = jsonUserObject.getString(UserTable.U_IS_FINISH);
			//订单是否完成
			userPreference.setU_IS_FINISH(isfinish);
			String ispay = jsonUserObject.getString(UserTable.U_IS_PAY);
			//是否完成支付
			userPreference.setU_IS_PAY(ispay);
			String name = jsonUserObject.getString(UserTable.STU_NAME);
			//该用户的姓名
			userPreference.setU_NAME(name);
//			String cardNum = jsonUserObject.getString(UserTable.STU_CARD_NUM);
			String college = jsonUserObject.getString(UserTable.STU_COLLEGE);
			//该用户所在的学校
			userPreference.setU_COLLEGE(college);
			String isVip = jsonUserObject.getString(UserTable.U_IS_VIP);
			//该用户是否为VIP
			userPreference.setU_is_vip(isVip);
			// String create_time =
			// jsonUserObject.getString(UserTable.U_CREATE_TIME);
			// Date date = DateTimeTools.StringToDate(create_time);
			userPreference.setUserLogin(true);
			//该用户的卡号
//			userPreference.setU_CARD_NUM(cardNum);
			userPreference.printUserInfo();
			// userPreference.setU_CreatTime(date);
			String ismessage = jsonUserObject.getString(UserTable.U_IS_MESSAGE);
			// 该用户是否有新消息
			userPreference.setU_IS_MESSAGE(ismessage);

		} catch (JSONException e) {
			e.printStackTrace();
			LogTool.e("存储用户全部信息有误");
		}
	}
	
}
