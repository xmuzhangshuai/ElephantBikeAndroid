package com.xxn.elephantbike.ui;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants.WeChatConfig;
import com.xxn.elephantbike.customewidget.CustomProgressDialog;
import com.xxn.elephantbike.customewidget.CustomTipDialog;
import com.xxn.elephantbike.pay.PayResult;
import com.xxn.elephantbike.ui.AccountSubmitActivity.MyReceiver;
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
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WalletRechargeActivity extends BaseActivity {

	private View navLeftBtn; // 后退按钮
	private ImageView selectedBtn;// 微信支付
	private ImageView unselectedBtn;// 支付宝支付
	private RelativeLayout weixinLayout;
	private RelativeLayout zfbLayout;
	private UserPreference userPreference;
	private CustomProgressDialog cLoadingDialog;

	private Button rechargeSubmit;
	private EditText moneyInputMuch;
	private OrderPreference orderPreference;
	private int rechargeType = 0;
	private String balance = "0";
	final IWXAPI api = WXAPIFactory.createWXAPI(this, null);
	private String outTradeNo = "";
	String fee, rechargeFee = "";
	private boolean isRun = false;
	private static final int SDK_PAY_FLAG = 1;
	private CustomTipDialog cpd = null; // 私有的对话框
	private TextView nav_text, money_text, weixin_text, weixin_text_tip, zfb_text, zfb_text_tip;

	/** 输入框小数的位数 */
	private static final int DECIMAL_DIGITS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_wallet_recharge);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		api.registerApp(WeChatConfig.APP_ID);

		findViewById();
		setTypeface();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, WalletRechargeActivity.this);
	}

	/**
	 * @Title: setTypeface
	 * @Description:
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		money_text.setTypeface(tf);
		moneyInputMuch.setTypeface(tf);
		rechargeSubmit.setTypeface(tf);
		weixin_text.setTypeface(tf);
		weixin_text_tip.setTypeface(tf);
		zfb_text.setTypeface(tf);
		zfb_text_tip.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		selectedBtn = (ImageView) findViewById(R.id.selected_button);
		unselectedBtn = (ImageView) findViewById(R.id.unselected_button);
		weixinLayout = (RelativeLayout) findViewById(R.id.weixin_Layout);
		zfbLayout = (RelativeLayout) findViewById(R.id.zfb_Layout);
		rechargeSubmit = (Button) findViewById(R.id.recharge_next_submit);
		moneyInputMuch = (EditText) findViewById(R.id.money_input);
		nav_text = (TextView) findViewById(R.id.nav_text);
		money_text = (TextView) findViewById(R.id.money_text);
		weixin_text = (TextView) findViewById(R.id.weixin_text);
		weixin_text_tip = (TextView) findViewById(R.id.weixin_text_tip);
		zfb_text = (TextView) findViewById(R.id.zfb_text);
		zfb_text_tip = (TextView) findViewById(R.id.zfb_text_tip);
	}

	/**
	 * 设置小数位数控制
	 */
	InputFilter lengthfilter = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			// 删除等特殊字符，直接返回
			if ("".equals(source.toString())) {
				return null;
			}
			String dValue = dest.toString();
			String[] splitArray = dValue.split("\\.");
			if (splitArray.length > 1) {
				String dotValue = splitArray[1];
				int diff = dotValue.length() + 1 - DECIMAL_DIGITS;
				if (diff > 0) {
					return source.subSequence(start, end - diff);
				}
			}
			return null;
		}
	};

	@Override
	protected void initView() {
		getMoneyRemainMuch(userPreference.getU_tel());
		// 首先对金额输入框中的金额输入框进行添加过滤器操作
		// moneyInputMuch.addTextChangedListener(mTextWatcher);
		moneyInputMuch.setFilters(new InputFilter[] { lengthfilter });
		/*
		 * moneyInputMuch.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * if (isRun) {// 这几句要加，不然每输入一个值都会执行两次onTextChanged()，导致堆栈溢出，原因不明 isRun
		 * = false; return; } // isRun = true; String finalString =
		 * s.toString(); if (!finalString.contains(".")) { finalString =
		 * finalString + ".00"; } else if
		 * (finalString.substring(finalString.indexOf("."),
		 * finalString.length()).length() == 1) { finalString = finalString +
		 * "0"; } moneyInputMuch.setText(finalString); //
		 * moneyInputMuch.setSelection(finalString.length()); }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub }
		 * 
		 * @Override public void afterTextChanged(Editable s) { // TODO
		 * Auto-generated method stub // 该方法中反应出改变输入框之后的所有字符数 } });
		 */
		// TODO Auto-generated method stub
		navLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		weixinLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * Drawable selabledrawable = selectedBtn.getBackground();
				 * Drawable unselDrawable = getResources().getDrawable(
				 * R.drawable.unselected_button); Drawable selectedDrawable =
				 * getResources().getDrawable( R.drawable.selected_button); if
				 * (unselDrawable.equals(selabledrawable))
				 * selectedBtn.setImageDrawable(selectedDrawable);
				 */
				unselectedBtn.setBackgroundResource(R.drawable.unselected_button);
				selectedBtn.setBackgroundResource(R.drawable.selected_button);
				rechargeType = 0;
			}
		});
		zfbLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				unselectedBtn.setBackgroundResource(R.drawable.selected_button);
				selectedBtn.setBackgroundResource(R.drawable.unselected_button);
				rechargeType = 1;
			}
		});

		rechargeSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 不允许点击两次
				rechargeSubmit.setClickable(false);
				rechargeFee = moneyInputMuch.getText().toString();
				if ("".equals(rechargeFee)) {
					ToastTool.showLong(WalletRechargeActivity.this, "请填写充值金额");
				} else if (Float.parseFloat(balance.toString()) + Float.parseFloat(rechargeFee.toString()) < 100) {
					if (rechargeType == 0) {
						// ToastTool.showLong(WalletRechargeActivity.this,
						// "暂时不支持微信充值!");
						// String totalfee =
						// moneyInputMuch.getText().toString();
						// wxPay(userPreference.getU_tel(), totalfee);
						rechargeFee = moneyInputMuch.getText().toString();
						// 告诉服务器充值金额
						// recharge(userPreference.getU_tel(), rechargeFee);
						wxRechargePay(userPreference.getU_tel(), rechargeFee);
					} else {
						getOrderInfo(userPreference.getU_tel(), "大象单车付款", "用车消费", rechargeFee,
								orderPreference.getBike_ID());
					}
				} else {
					ToastTool.showShort(WalletRechargeActivity.this, "应使余额少于100元，请修改充值金额");
				}
			}
		});

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
			WalletRechargeActivity.this.finish();
		}

		public MyReceiver() {
			// 做初始化
			System.out.println("MyReceiver init...");
		}
	}

	/**
	 * create the order info. 创建订单信息，从服务器端获得orderId 订单支付api/pay/alipayrecharge
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
		AsyncHttpClientTool.post("api/pay/alipayrecharge", params, responseHandler);

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
		LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, false);
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
					// 告诉服务器充值金额（不需要我们来告诉）
					// recharge(userPreference.getU_tel(), rechargeFee);
					// 再去调用支付宝查询订单接口查询是否支付成功，以提高安全性
					queryOrderState(outTradeNo);
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(WalletRechargeActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
					} else if (TextUtils.equals(resultStatus, "4000")) {
						Toast.makeText(WalletRechargeActivity.this, "手机没有安装支付宝，请安装后付款", Toast.LENGTH_SHORT).show();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(WalletRechargeActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
			default:
				break;
			}
		};
	};

	private void sendZfbPay(final String payInfo) {
		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(WalletRechargeActivity.this);
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
	 * 请求生成支付订单，进行支付请求（调用微信进行支付付款）
	 * 
	 * @param phone
	 * @param totalfee
	 */
	private void wxRechargePay(String phone, String totalfee) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("totalfee", totalfee);
		cLoadingDialog = new CustomProgressDialog(WalletRechargeActivity.this, "");
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
					outTradeNo = jsonObject.getString("out_trade_no"); // 拿到订单号,方便后期查询
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
						// 可再点击一次
						rechargeSubmit.setClickable(true);
//						WalletRechargeActivity.this.finish();
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
		AsyncHttpClientTool.post("api/pay/wxpayrecharge", params, responseHandler);
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
						cpd = new CustomTipDialog.Builder(WalletRechargeActivity.this).create(0);
						cpd.show();
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								// 钱包充值结束，重新刷新我的钱包页面
								WalletRechargeActivity.this.finish();
								Toast.makeText(WalletRechargeActivity.this, "支付成功！", Toast.LENGTH_SHORT).show();
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

	/**
	 * 充值费用
	 * 
	 * @Title: recharge
	 * @Description: TODO
	 * @param phone
	 * @param rechargeFee
	 * @return: void
	 */
	private void recharge(String phone, String rechargeFee) {

		RequestParams params = new RequestParams();
		params.put("phone", phone);
		params.put("value", rechargeFee);
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, true);
				super.onStart();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, false);
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					String message = jsonObject.getString("message");
					if (status.equals("success")) {
						ToastTool.showShort(WalletRechargeActivity.this, message);
						WalletRechargeActivity.this.finish();
						// AppManager.getInstance().killActivity(MoneyConsumerActivity.class);
						// startActivity(new Intent(WalletRechargeActivity.this,
						// MoneyConsumerActivity.class));
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
		AsyncHttpClientTool.post("api/money/recharge", params, responseHandler);
	}

	/**
	 * 根据手机号来获取钱包余额
	 */
	private void getMoneyRemainMuch(String phone) {

		RequestParams params = new RequestParams();
		params.put("phone", phone);
		params.put("access_token", userPreference.getAccess_token());

		LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, true);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(WalletRechargeActivity.this, false);
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					// 如果获取成功时，进行以下设值操作
					if (status.equals("success")) {
						// 钱包余额
						balance = jsonObject.getString("balance");
						// 获取金额，将moneyRemainMuch设置余额
					} else {
						LogTool.e("设置金额失败！");
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
		AsyncHttpClientTool.post("api/money/balance", params, responseHandler);
	}

}
