package com.xxn.elephantbike.wxapi;

import org.apache.http.Header;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants;
import com.xxn.elephantbike.customewidget.CustomProgressDialog;
import com.xxn.elephantbike.customewidget.CustomTipDialog;
import com.xxn.elephantbike.ui.AccountAndPersonActivity;
import com.xxn.elephantbike.ui.CaptureActivity;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

	private IWXAPI api;
	private CustomTipDialog cpd = null; // 私有的对话框
	private UserPreference userPreference;
	private OrderPreference orderPreference;
	private CustomProgressDialog cLoadingDialog;
	private View navLeftBtn; // 后退按钮

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pay_result);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();

		api = WXAPIFactory.createWXAPI(this, Constants.WeChatConfig.APP_ID);
		api.handleIntent(getIntent(), this);

		findViewById();
		initView();
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		navLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			// AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// builder.setTitle("提示");
			// builder.setMessage(String.valueOf(resp.errCode));
			// builder.show();
			// 如果调用成功则去查询订单
			if ("0".equals(String.valueOf(resp.errCode))) {
				// 支付成功去查询结果过
				wxOrderQuery();
			} else if ("-2".equals(String.valueOf(resp.errCode))) {
				WXPayEntryActivity.this.finish();
				ToastTool.showLong(WXPayEntryActivity.this, "您已取消支付");
			} else {
				WXPayEntryActivity.this.finish();
				ToastTool.showLong(WXPayEntryActivity.this, "支付失败");
			}
		}
	}

	/**
	 * （微信）查询订单的支付情况
	 * 
	 * @Title: wxOrderQuery
	 * @Description: TODO
	 * @return: void
	 */
	private void wxOrderQuery() {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("out_trade_no", userPreference.getWxOutTradeNo());
		// 支付等待效果加载ing
		// LoadingPopTool.popLoadingStyle(WXPayEntryActivity.this, true);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);

				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 发送广播去做UI操作
					WXPayEntryActivity.this.finish();
					Intent intent = new Intent();
					intent.putExtra("result","success");
					intent.setAction("android.intent.action.wxpayreslut");// action与接收器相同
					sendBroadcast(intent);
				} else {
					LogTool.e("查询支付情况失败！" + message);
					WXPayEntryActivity.this.finish();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
					}
				}
			}
		};
		AsyncHttpClientTool.post("api/pay/wxorderquery", params, responseHandler);
	}

	/**
	 * 调用该接口还车支付费用完成，告诉服务器该订单结束
	 * 
	 * @Title: returnPay
	 * @Description: TODO
	 * @param bikeid
	 * @param phone
	 * @param ismissing
	 * @param paymode
	 * @param payModeType
	 * @return: void
	 */
	private void returnPay(String bikeid, String phone, String ismissing, String paymode, final int payModeType) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("paymode", paymode);
		params.put("ismissing", ismissing);
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);

				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 支付等待框结束
					LoadingPopTool.popLoadingStyle(WXPayEntryActivity.this, false);
					if (payModeType == 1) {
						// 如果是微信支付
						// wxOrderQuery(userPreference.getU_tel());
						// 支付成功时的对话框提示
						cpd = new CustomTipDialog.Builder(WXPayEntryActivity.this).create(0);
						cpd.show();
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								WXPayEntryActivity.this
										.startActivity(new Intent(WXPayEntryActivity.this, CaptureActivity.class));
								WXPayEntryActivity.this.finish();
							}
						}, 2000);
					}
				} else {
					LogTool.e("还车支付费用失败！" + message);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
					}
				}
			}
		};
		AsyncHttpClientTool.post("api/money/returnpay", params, responseHandler);
	}

	/**
	 * 还车支付费用(此处为未正常还车-----丢车)
	 * 
	 * @Title: returnLostBikePay
	 * @Description: TODO
	 * @param bikeid
	 * @param phone
	 * @param paymode
	 * @return: void
	 */
	private void returnLostBikePay(String bikeid, String phone, String paymode) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("paymode", paymode);
		params.put("ismissing", "1");
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				// JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 支付成功时的对话框提示
					cpd = new CustomTipDialog.Builder(WXPayEntryActivity.this).create(0);
					cpd.show();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							// execute the task
							cpd.dismiss();
							WXPayEntryActivity.this
									.startActivity(new Intent(WXPayEntryActivity.this, CaptureActivity.class));
							// 结束掉计费页面，防止按返回键时显示出来
							AppManager.getInstance().killActivity(AccountAndPersonActivity.class);
							WXPayEntryActivity.this.finish();
						}
					}, 2000);

				} else {
					LogTool.e("支付费用失败！" + message);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
					}
				}
			}
		};
		AsyncHttpClientTool.post("api/money/returnpay", params, responseHandler);
	}

}