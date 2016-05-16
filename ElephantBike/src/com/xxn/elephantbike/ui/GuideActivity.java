package com.xxn.elephantbike.ui;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants;
import com.xxn.elephantbike.table.ShareTable;
import com.xxn.elephantbike.ui.NoticeMessActivity.CustomListAdapter;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.ServerUtil;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author lzjing 启动页（欢迎页），2000ms然后进入APP主界面
 */
public class GuideActivity extends BaseActivity {

	private UserPreference userPreference;
	private SharePreference sharePreference;// 共享变量

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		findViewById();
		initView();
		Handler h = new Handler();
		h.postDelayed(new splashhandler(), 2000);
	}

	class splashhandler implements Runnable {

		public void run() {
			// 用于判断用户是否登录
			userPreference = BaseApplication.getInstance().getUserPreference();
			sharePreference = BaseApplication.getInstance().getSharePreference();
			/*
			 * if(userPreference.getUserLogin())
			 * System.out.println("userPreference.getUserLogin()" + " = 1");
			 * else System.out.println("userPreference.getUserLogin()" + " = 0"
			 * );
			 */
			// userPreference.setU_IS_FINISH("0");
			// userPreference.setU_IS_FROZEN("0");
			// userPreference.setU_IS_PAY("0");
			// userPreference.setUserLogin(true);
			// userPreference.setU_IS_PAY("1");
			// 从服务器中去拉活动（公告）状态，并存在本地
			getTopic(GuideActivity.this);
			if (userPreference.getUserLogin()) {
				// 1、如果是已经登陆过
				// if (NetworkUtils.isNetworkAvailable(GuideActivity.this))
				// 如果网络可用，则跳转到扫描页，如果没网络保持原启动页
				ServerUtil.getInstance().login(GuideActivity.this, CaptureActivity.class);
			} else {
				// 如果用户没有登录过或者已经注销，则是直接进入扫描页
				startActivity(new Intent(GuideActivity.this, CaptureActivity.class));
				finish();
			}
		}

	}
	
	// 去获取服务器的公告信息
	public void getTopic(final Context context) {

		// 1 代表弹窗 2代表计费页面 3 代表个人中心
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("type", "1");
		// 向服务器发送请求获取公告（活动）状态
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				if (status.equals("success")) {
					// 将服务器传回来的数据进行处理,存储公告的图片地址和链接地址
					saveShareData(jsonObject, 1);
				} else {
					// 返回状态不正确是将失败标志设置到本地
					sharePreference.setActivity_status(status);
					LogTool.e("status" + status);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(context, "api/act/topic", params, responseHandler);

		// 1 代表弹窗 2代表计费页面 3 代表个人中心
		RequestParams params_1 = new RequestParams();
		params_1.put("type", "2");
		// 向服务器发送请求获取公告（活动）状态
		TextHttpResponseHandler responseHandler_1 = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				if (status.equals("success")) {
					// 将服务器传回来的数据进行处理,存储公告的图片地址和链接地址
					saveShareData(jsonObject, 2);
				} else {
					// 返回状态不正确是将失败标志设置到本地
					sharePreference.setActivity_status(status);
					LogTool.e("status" + status);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(context, "api/act/topic", params_1, responseHandler_1);

		// 1 代表弹窗 2代表计费页面 3 代表个人中心
		RequestParams params_2 = new RequestParams();
		params_2.put("type", "3");
		// 向服务器发送请求获取公告（活动）状态
		TextHttpResponseHandler responseHandler_2 = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				if (status.equals("success")) {
					// 将服务器传回来的数据进行处理,存储公告的图片地址和链接地址
					saveShareData(jsonObject, 3);
				} else {
					// 返回状态不正确是将失败标志设置到本地
					sharePreference.setActivity_status(status);
					LogTool.e("status" + status);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(context, "api/act/topic", params_2, responseHandler_2);
	}

	/**
	 * 存储共享的数据
	 */
	private void saveShareData(final JSONObject jsonUserObject, int type) {
		// TODO Auto-generated method stub
		try {
			if (type == 1) {
				String status = jsonUserObject.getString(ShareTable.ACTIVITY_STATUS);
				String imageurl = jsonUserObject.getString(ShareTable.IMAGE_URL);
				String linkurl = jsonUserObject.getString(ShareTable.LINK_URL);

				// if(linkurl.contains("http://")){
				// linkurl = linkurl.replace("http://", "");
				// }

				sharePreference.setActivity_status(status);
				sharePreference.setImage_url(Constants.AppliactionServerIP + imageurl);
				sharePreference.setLink_url(linkurl);
				sharePreference.printUserInfo();
			} else if (type == 2) {
				String status_1 = jsonUserObject.getString(ShareTable.ACTIVITY_STATUS);
				String imageurl_1 = jsonUserObject.getString(ShareTable.IMAGE_URL);
				String linkurl_1 = jsonUserObject.getString(ShareTable.LINK_URL);

				// if(linkurl.contains("http://")){
				// linkurl = linkurl.replace("http://", "");
				// }

				sharePreference.setActivity_status_1(status_1);
				sharePreference.setImage_url_1(Constants.AppliactionServerIP + imageurl_1);
				sharePreference.setLink_url_1(linkurl_1);
				sharePreference.printUserInfo();
			} else {
				String status_2 = jsonUserObject.getString(ShareTable.ACTIVITY_STATUS);
				String imageurl_2 = jsonUserObject.getString(ShareTable.IMAGE_URL);
				String linkurl_2 = jsonUserObject.getString(ShareTable.LINK_URL);

				// if(linkurl.contains("http://")){
				// linkurl = linkurl.replace("http://", "");
				// }

				sharePreference.setActivity_status_2(status_2);
				sharePreference.setImage_url_2(Constants.AppliactionServerIP + imageurl_2);
				sharePreference.setLink_url_2(linkurl_2);
				sharePreference.printUserInfo();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogTool.e("存储共享信息有误");
		}
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub

	}
}
