/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: PayForBikeLostActivity.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.ui 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年4月6日 下午5:26:17 
 * @version: V1.0   
 */
package com.xxn.elephantbike.ui;

import java.text.DecimalFormat;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants.WeChatConfig;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.customewidget.CustomProgressDialog;
import com.xxn.elephantbike.customewidget.CustomTipDialog;
import com.xxn.elephantbike.pay.PayResult;
import com.xxn.elephantbike.ui.WalletRechargeActivity.MyReceiver;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: PayForBikeLostActivity
 * @Description:
 * @author: Administrator
 * @date: 2016年4月6日 下午5:26:17
 */
public class PayForBikeLostActivity extends BaseActivity implements OnClickListener {
	private ImageView eleSelectedBtn;// 大象钱包支付
	private ImageView wxSelectedBtn;// 微信支付
	private ImageView zfbSelectedBtn;// 支付宝支付
	private RelativeLayout elephantLayout;
	private RelativeLayout weixinLayout;
	private RelativeLayout zfbLayout;
	private UserPreference userPreference;
	private OrderPreference orderPreference;
	private Button payConfirmBtn;// 确认支付
	private View questionMark;
	private TextView bikeNum; // 单车编号
	private ImageView vipLogo;// ImageView的logo
	private TextView feeMuch;// 费用总计
	private CustomTipDialog cpd = null; // 私有的对话框
	private ImageView personInfoBtn;
	int fee = 300;// 费用
	private String outTradeNo = "";
	String balance = "0"; // 余额
	private int payType = 0;// 记录支付的方式
	private IWXAPI api;
	private static final int SDK_PAY_FLAG = 1;
	String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";
	private TextView account_flag, bike_title, fee_title, elephant_text, elephant_text_tip, weixin_text,
			weixin_text_tip, zfb_text, zfb_textx_tip, problem_tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pay_for_bike_lost);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(PayForBikeLostActivity.this, null);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		// 将该app注册到微信
		api.registerApp(WeChatConfig.APP_ID);
		// 确认单车丢失后不再去后台请求单车费用和时间
		AccountAndPersonActivity.isContinue = false;
		findViewById();
		setTypeface();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, PayForBikeLostActivity.this);
	}

	/**
	 * @Title: setTypeface
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		account_flag.setTypeface(tf);
		bike_title.setTypeface(tf);
		bikeNum.setTypeface(tf);
		fee_title.setTypeface(tf);
		feeMuch.setTypeface(tf);
		elephant_text.setTypeface(tf);
		elephant_text_tip.setTypeface(tf);
		weixin_text.setTypeface(tf);
		weixin_text_tip.setTypeface(tf);
		zfb_text.setTypeface(tf);
		zfb_textx_tip.setTypeface(tf);
		payConfirmBtn.setTypeface(tf);
		problem_tip.setTypeface(tf);
	}

	// 屏蔽返回按键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void findViewById() {
		vipLogo = (ImageView) this.findViewById(R.id.vip_logo);
		elephantLayout = (RelativeLayout) findViewById(R.id.elephant_Layout);
		weixinLayout = (RelativeLayout) findViewById(R.id.weixin_Layout);
		zfbLayout = (RelativeLayout) findViewById(R.id.zfb_Layout);
		eleSelectedBtn = (ImageView) findViewById(R.id.ele_selected_button);
		wxSelectedBtn = (ImageView) findViewById(R.id.wx_selected_button);
		zfbSelectedBtn = (ImageView) findViewById(R.id.zfb_selected_button);
		payConfirmBtn = (Button) findViewById(R.id.pay_confirm);
		questionMark = (View) findViewById(R.id.question_mark);
		feeMuch = (TextView) findViewById(R.id.fee_much);
		bikeNum = (TextView) findViewById(R.id.bike_num);
		personInfoBtn = (ImageView) this.findViewById(R.id.account_person_info_btn);
		account_flag = (TextView) findViewById(R.id.account_flag);
		bike_title = (TextView) findViewById(R.id.bike_title);
		fee_title = (TextView) findViewById(R.id.fee_title);
		elephant_text = (TextView) findViewById(R.id.elephant_text);
		elephant_text_tip = (TextView) findViewById(R.id.elephant_text_tip);
		weixin_text = (TextView) findViewById(R.id.weixin_text);
		weixin_text_tip = (TextView) findViewById(R.id.weixin_text_tip);
		zfb_text = (TextView) findViewById(R.id.zfb_text);
		zfb_textx_tip = (TextView) findViewById(R.id.zfb_textx_tip);
		problem_tip = (TextView) findViewById(R.id.problem_tip);
	}

	@Override
	protected void initView() {

		elephantLayout.setOnClickListener(this);
		weixinLayout.setOnClickListener(this);
		zfbLayout.setOnClickListener(this);
		payConfirmBtn.setOnClickListener(this);
		questionMark.setOnClickListener(this);

		personInfoBtn.setOnClickListener(this);
		// 确认丢车标志
		orderPreference.setBIKE_LOST_FLAG(true);
		// 加载单车丢失的费用，取得单车丢失费用，该操作会操作订单
		get_missBFee(orderPreference.getBike_ID(), userPreference.getU_tel());
		bikeNum.setText(orderPreference.getBike_ID());
		// getBalance(userPreference.getU_tel());

		// 注册接收器
		MyReceiver receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.wxpayreslut");
		this.registerReceiver(receiver, filter);
	}

	public class MyReceiver extends BroadcastReceiver {
		// 自定义一个广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("OnReceiver");
			Bundle bundle = intent.getExtras();
			String result = bundle.getString("result");
			LogTool.i("result:", result);
			// 处理接收到的内容,做初始化跳转
			PayForBikeLostActivity.this.finish();
		}

		public MyReceiver() {
			// 做初始化
			System.out.println("MyReceiver init...");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.question_mark:
			final CustomPopDialog cp = new CustomPopDialog.Builder(PayForBikeLostActivity.this).create(4);
			cp.show();
			TextView confirmTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm);
			confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
				}
			});
			break;
		case R.id.account_person_info_btn:
			startActivity(new Intent(getApplicationContext(), PersonInfoActivity.class));
			break;
		case R.id.elephant_Layout:
			eleSelectedBtn.setBackgroundResource(R.drawable.selected_button);
			wxSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			zfbSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			payType = 0;
			break;
		case R.id.weixin_Layout:
			eleSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			wxSelectedBtn.setBackgroundResource(R.drawable.selected_button);
			zfbSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			payType = 1;
			break;
		case R.id.zfb_Layout:
			eleSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			wxSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			zfbSelectedBtn.setBackgroundResource(R.drawable.selected_button);
			payType = 2;
			break;
		case R.id.pay_confirm:
			getBalance(userPreference.getU_tel());
			break;
		default:
			break;
		}
	}

	/**
	 * @Title: getBalance
	 * @Description: 获取余额
	 * @return: void
	 */
	private void getBalance(String phone) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, false);

				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						balance = jsonObject.getString("balance");
						String fromActivityTo = getIntent().getStringExtra("fromActivityTo");
						if ("BikeLostActivity".equals(fromActivityTo)) {
							if (payType == 0) {
								if (Double.valueOf(balance) > 0 && Double.valueOf(balance) > Double.valueOf(fee)) {
									returnLostBikePay(orderPreference.getBike_ID() + "", userPreference.getU_tel(),
											"大象钱包");
								} else {
									// ToastTool.showLong(PayForBikeLostActivity.this,
									// "余额不足，请先充值或者稍后再试！");
									final CustomPopDialog cp = new CustomPopDialog.Builder(PayForBikeLostActivity.this)
											.create(8);
									cp.show();
									TextView confirmTv = (TextView) CustomPopDialog.getLayout()
											.findViewById(R.id.dialog_comfirm); // 确认退出登录
									TextView dialogCancel = (TextView) CustomPopDialog.getLayout()
											.findViewById(R.id.dialog_cancel); // 取消退出登录
									confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
										@Override
										public void onClick(View v) {
											PayForBikeLostActivity.this.startActivity(new Intent(
													PayForBikeLostActivity.this, WalletRechargeActivity.class));
										}
									});
									dialogCancel.setOnClickListener(new android.view.View.OnClickListener() {
										@Override
										public void onClick(View v) {
											cp.cancel();
										}
									});
								}
							} else if (payType == 1) {
								wxPayOrder(userPreference.getU_tel(), orderPreference.getBike_ID());
							} else if (payType == 2) {
								getOrderInfo(userPreference.getU_tel(), "大象单车付款", "用车消费", fee + "",
										orderPreference.getBike_ID());
							}
						}
					} else {
						LogTool.e("获取余额失败！");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable) {
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {

					}
				}
			}
		};
		AsyncHttpClientTool.post("api/money/balance", params, responseHandler);

	}

	/**
	 * 请求生成支付订单，进行支付请求（调用微信进行支付付款）
	 * 
	 * @param phone
	 * @param totalfee
	 */
	private void wxPayOrder(String phone, String bikeid) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("bikeid", bikeid);
		cLoadingDialog = new CustomProgressDialog(PayForBikeLostActivity.this, "");
		if (!cLoadingDialog.isShowing())
			cLoadingDialog.show();

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				if (cLoadingDialog.isShowing())
					cLoadingDialog.cancel();
				String status = jsonTool.getStatus();
				try {
					String appid = jsonObject.getString("appid");
					String partnerid = jsonObject.getString("partnerid");
					String prepayid = jsonObject.getString("prepayid");
					String packageKey = jsonObject.getString("package");
					String noncestr = jsonObject.getString("noncestr");
					String timestamp = jsonObject.getString("timestamp");
					String sign = jsonObject.getString("sign");
					String outTradeNo = jsonObject.getString("out_trade_no"); // 拿到订单号,方便后期查询
					// 将订单号存起来
					userPreference.setWxOutTradeNo(outTradeNo);
					if (status.equals("success")) {
						PayReq req = new PayReq();
						req.appId = appid;
						req.partnerId = partnerid;
						req.prepayId = prepayid;
						req.nonceStr = noncestr;
						req.timeStamp = timestamp;
						req.packageValue = packageKey;

						req.sign = sign;
						// req.extData = "app data"; // optional
						// api =
						// WXAPIFactory.createWXAPI(WalletRechargeActivity.this,
						// appid);
						// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
						// 将该app注册到微信
						api.registerApp(appid);
						api.sendReq(req);
//						PayForBikeLostActivity.this.finish();
					} else {
						LogTool.e("进行支付请求失败！");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		AsyncHttpClientTool.post("api/pay/wxpayorder", params, responseHandler);
	}

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				/**
				 * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
				 * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
				 * docType=1) 建议商户依赖异步通知
				 */
				String resultInfo = payResult.getResult();// 同步返回需要验证的信息

				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					Toast.makeText(PayForBikeLostActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
					// 支付宝支付
					queryOrderState(outTradeNo);
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(PayForBikeLostActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
					} else if (TextUtils.equals(resultStatus, "4000")) {
						Toast.makeText(PayForBikeLostActivity.this, "手机没有安装支付宝，请安装后付款", Toast.LENGTH_SHORT).show();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(PayForBikeLostActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
			default:
				break;
			}
		};
	};

	/**
	 * create the order info. 创建订单信息，从服务器端获得orderId 订单支付
	 * 
	 * @Title: getOrderInfo
	 * @Description: TODO
	 * @param phone
	 * @param subject
	 * @param body
	 * @param month
	 * @return: void
	 */
	private void getOrderInfo(String phone, String subject, String body, String totalfee, String bikeid) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("bikeid", bikeid);
		params.put("subject", subject);
		params.put("body", body);
		params.put("totalfee", totalfee);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				try {
					String param = jsonObject.getString("param");
					String sign = jsonObject.getString("sign");
					String sign_type = jsonObject.getString("sign_type");
					outTradeNo = jsonObject.getString("out_trade_no"); // 拿到订单号,方便后期查询
					String payInfo = param + "&sign=\"" + sign + "\"&" + "sign_type=\"" + sign_type + "\"";
					LogTool.i(payInfo);

					// 向支付宝发送支付请求
					sendZfbPay(payInfo);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogTool.e("获取支付宝支付信息出错！");
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
						LogTool.e("调用支付宝支付出问题！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/pay/alipaypay", params, responseHandler);

	}

	/**
	 * 查询订单结果
	 * 
	 * @Title: queryOrderState
	 * @Description: TODO
	 * @param orderId
	 * @return: void
	 */
	private void queryOrderState(String orderId) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("out_trade_no", orderId);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						String message = jsonObject.getString("message");
						LogTool.i(message);
						// 支付成功时的对话框提示
						cpd = new CustomTipDialog.Builder(PayForBikeLostActivity.this).create(0);
						cpd.show();
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								PayForBikeLostActivity.this
										.startActivity(new Intent(PayForBikeLostActivity.this, CaptureActivity.class));
								PayForBikeLostActivity.this.finish();
							}
						}, 2000);
					} else {
						LogTool.i("查询结果：订单支付成功！");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
						LogTool.e("查询结果：订单支付失败！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/pay/alipayquery", params, responseHandler);
	}

	private void sendZfbPay(final String payInfo) {
		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(PayForBikeLostActivity.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo, true);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();

	}

	/**
	 * create the order info. 创建订单信息，从服务器端获得orderId
	 * 
	 * @Title: getOrderInfo
	 * @Description: TODO
	 * @param phone
	 * @param subject
	 * @param body
	 * @param month
	 * @return: void
	 */
	private void getOrderInfo(String phone, String subject, String body, String month) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("subject", subject);
		params.put("body", body);
		params.put("month", month);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					String param = jsonObject.getString("param");
					String sign = jsonObject.getString("sign");
					String sign_type = jsonObject.getString("sign_type");
					String payInfo = param + "&sign=\"" + sign + "\"&" + "sign_type=\"" + sign_type + "\"";
					LogTool.e(payInfo);

					// 向支付宝发送支付请求
					sendZfbPay(payInfo);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogTool.e("获取支付宝支付信息出错！");
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
						LogTool.e("调用支付宝支付出问题！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/pay/alipayorder", params, responseHandler);

	}

	/**
	 * 请求生成支付订单，进行支付请求（调用微信进行支付付款）
	 * 
	 * @param phone
	 * @param totalfee
	 */
	private void wxPay(String phone, String totalfee) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("totalfee", totalfee);
		LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, false);
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
					Toast.makeText(PayForBikeLostActivity.this, String.valueOf(isPaySupported), Toast.LENGTH_SHORT)
							.show();
					String appid = jsonObject.getString("appid");
					String partnerid = jsonObject.getString("partnerid");
					String prepayid = jsonObject.getString("prepayid");
					String packageKey = jsonObject.getString("package");
					String noncestr = jsonObject.getString("noncestr");
					String timestamp = jsonObject.getString("timestamp");
					String sign = jsonObject.getString("sign");
					if (status.equals("success")) {
						PayReq req = new PayReq();

						req.appId = appid;
						req.partnerId = partnerid;
						req.prepayId = prepayid;
						req.nonceStr = packageKey;
						req.timeStamp = noncestr;
						req.packageValue = timestamp;
						req.sign = sign;
						// req.extData = "app data"; // optional
						api.sendReq(req);
					} else {
						LogTool.e("进行支付请求失败！");
					}
				} catch (JSONException e) {
					e.printStackTrace();
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
		AsyncHttpClientTool.post("api/pay/wxpayorder", params, responseHandler);
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
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("paymode", paymode);
		params.put("ismissing", "1");
		params.put("access_token", userPreference.getAccess_token());
		LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				// JSONObject jsonObject = jsonTool.getJsonObject();
				LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, false);
				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 支付成功时的对话框提示
					cpd = new CustomTipDialog.Builder(PayForBikeLostActivity.this).create(0);
					cpd.show();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							// execute the task
							cpd.dismiss();
							PayForBikeLostActivity.this
									.startActivity(new Intent(PayForBikeLostActivity.this, CaptureActivity.class));
							// 结束掉计费页面，防止按返回键时显示出来
							// AppManager.getInstance().killActivity(AccountAndPersonActivity.class);
							PayForBikeLostActivity.this.finish();
						}
					}, 2000);

				} else {
					LogTool.e("丢车支付费用失败！" + message);
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

	/**
	 * 取得单车丢失费用，该操作会操作订单
	 * 
	 * @Title: upload_missBFee
	 * @Description: 单车丢失
	 * @return: void
	 */
	private void get_missBFee(String bikeid, String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(PayForBikeLostActivity.this, false);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				try {
					feeMuch.setText("￥" + 300);// 做特殊处理
					if (jsonTool.getStatus().equals("success")) {
						String feeStr = jsonObject.getString("fee");
						fee = Integer.parseInt(feeStr);
						DecimalFormat df = new DecimalFormat("0.00");// 格式化小数
						String feeString = df.format(fee);// 返回的是String类型
						feeMuch.setText("￥" + feeString);
					} else {
						// 做特殊处理
						// ToastTool.showLong(getApplicationContext(),
						// "获取单车丢失费用失败！");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable) {
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						ToastTool.showLong(getApplicationContext(), "服务器错误！");
					}
				}
			}

		};

		AsyncHttpClientTool.post("api/bike/missbikefee", params, responseHandler);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, PayForBikeLostActivity.this);
	}

}
