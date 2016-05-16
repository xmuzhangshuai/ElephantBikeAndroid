package com.xxn.elephantbike.ui;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants.WeChatConfig;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.customewidget.CustomProgressDialog;
import com.xxn.elephantbike.customewidget.CustomTipDialog;
import com.xxn.elephantbike.pay.PayResult;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.UserPreference;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccountSubmitActivity extends BaseActivity implements OnClickListener {

	private ImageView eleSelectedBtn;// 大象钱包支付
	private ImageView wxSelectedBtn;// 微信支付
	private ImageView zfbSelectedBtn;// 支付宝支付
	private RelativeLayout elephantLayout;
	private RelativeLayout weixinLayout;
	private RelativeLayout zfbLayout;
	private UserPreference userPreference;
	private OrderPreference orderPreference;
	private Button payConfirmBtn;
	private View questionMark;
	private TextView bikeNum; // 单车编号
	private ImageView vipLogo;// ImageView的logo
	private TextView feeMuch;// 费用总计
	private TextView useTimeMuch;// 使用时间
	private ImageView personInfoBtn;
	private final int CLOSE_ALERTDIALOG = 0; // 定义关闭对话框的动作信号标志
	private final int CLOSE_SAMPLE_VIEW = 0; // 定义关闭SampleView的动作信号标志
	private CustomTipDialog cpd = null; // 私有的对话框
	private int isPaySuccess = 0;

	private IWXAPI api;
	String fee = "0";// 费用
	String balance = "0"; // 余额
	private int payType = 0;// 记录支付的方式
	private String outTradeNo = "";
	private String AS_bikeid = "000000";
	String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";
	private static final int SDK_PAY_FLAG = 1;
	private TextView account_flag, bike_title, timelong_title, fee_title, elephant_text, elephant_text_tip, weixin_text,
			weixin_text_tip, zfb_text, zfb_text_tip, problem_tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_account_submit);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, WeChatConfig.APP_ID, false);
		// api = WXAPIFactory.createWXAPI(AccountSubmitActivity.this, null);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		// 将该app注册到微信
		api.registerApp(WeChatConfig.APP_ID);
		// 到计费页面时则停止想服务器请求时间和费用，避免增加服务器负担
		AccountAndPersonActivity.isContinue = false;

		findViewById();
		setTypeface();
		initView();

		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, AccountSubmitActivity.this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 将其设为可点击的状态
		personInfoBtn.setClickable(true);
		super.onResume();
	}

	/**
	 * @Title: setTypeface
	 * @Description:
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		account_flag.setTypeface(tf);
		bike_title.setTypeface(tf);
		bikeNum.setTypeface(tf);
		timelong_title.setTypeface(tf);
		useTimeMuch.setTypeface(tf);
		fee_title.setTypeface(tf);
		feeMuch.setTypeface(tf);
		elephant_text.setTypeface(tf);
		elephant_text_tip.setTypeface(tf);
		weixin_text.setTypeface(tf);
		weixin_text_tip.setTypeface(tf);
		zfb_text.setTypeface(tf);
		zfb_text_tip.setTypeface(tf);
		payConfirmBtn.setTypeface(tf);
	}

	/*
	 * // 屏蔽返回按键
	 * 
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { return true; } return
	 * super.onKeyDown(keyCode, event);
	 * 
	 * }
	 */

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
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
		useTimeMuch = (TextView) findViewById(R.id.use_time_much);
		bikeNum = (TextView) findViewById(R.id.bike_num);
		personInfoBtn = (ImageView) this.findViewById(R.id.account_person_info_btn);
		account_flag = (TextView) findViewById(R.id.account_flag);
		bike_title = (TextView) findViewById(R.id.bike_title);
		timelong_title = (TextView) findViewById(R.id.timelong_title);
		fee_title = (TextView) findViewById(R.id.fee_title);
		elephant_text = (TextView) findViewById(R.id.elephant_text);
		elephant_text_tip = (TextView) findViewById(R.id.elephant_text_tip);
		weixin_text = (TextView) findViewById(R.id.weixin_text);
		weixin_text_tip = (TextView) findViewById(R.id.weixin_text_tip);
		zfb_text = (TextView) findViewById(R.id.zfb_text);
		zfb_text_tip = (TextView) findViewById(R.id.zfb_text_tip);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		// problem_submit.setOnClickListener(this);
		elephantLayout.setOnClickListener(this);
		weixinLayout.setOnClickListener(this);
		zfbLayout.setOnClickListener(this);
		payConfirmBtn.setOnClickListener(this);
		questionMark.setOnClickListener(this);
		personInfoBtn.setOnClickListener(this);

		String fromActivityTo = getIntent().getStringExtra("fromActivityTo");
		// 直接由引导页或者由登录注册页过来
		if ("GuideActivity".equals(fromActivityTo) || "LoginOrRegisterActivity".equals(fromActivityTo)) {
			// 已经结束还车，但还未付款
			getCostandtime(userPreference.getU_tel());
			getBikeidandpass(userPreference.getU_tel());
			// 由于非正常跳入，因此需要获取到bikeid
			// if ("-000000".equals(AS_bikeid)) {
			// ToastTool.showLong(AccountSubmitActivity.this, "您可能需要重新登录一次！");
			// }
		}
		if ("AccountAndPersonActivity".equals(fromActivityTo)) {
			// 正常流程完成还车，获取金额和时间
			// getMoneyAndTime(AccountAndPersonActivity.bikeid + "",
			// userPreference.getU_tel(), "1");
			getMoneyAndTime(orderPreference.getBike_ID() + "", userPreference.getU_tel(), "1", "1");
		}
		// 处理遇到问题页面时候过来的跳转
		if ("ProblemUnableLockActivity".equals(fromActivityTo) || "ProblemPasswordActivity".equals(fromActivityTo)
				|| "ProblemPasswordActivity".equals(fromActivityTo)) {
			// 遇到问题，强制还车
			getMoneyAndTime(orderPreference.getBike_ID() + "", userPreference.getU_tel(), "1", "0");
		}

		// 1为表示已经得到了认证，此时显示是会员LOGO
		if ("1".equals(userPreference.getU_IS_FROZEN()) || "-1".equals(userPreference.getU_IS_FROZEN())
				|| "3".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			if ("1".equals(isVip)) {
				vipLogo.setImageResource(R.drawable.vip_logo);
			}
		}
		bikeNum.setText(orderPreference.getBike_ID());

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
			AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
			AccountSubmitActivity.this.finish();
		}

		public MyReceiver() {
			// 做初始化
			System.out.println("MyReceiver init...");
		}
	}

	/**
	 * 判断当前的余额是否充足，然后进行支付
	 * 
	 * @Title: getBalance
	 * @Description: TODO
	 * @return: void
	 */
	private void getBalance(String phone) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, true);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, false);
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						balance = jsonObject.getString("balance");
						// 正常还车
						String fromActivityTo = getIntent().getStringExtra("fromActivityTo");
						if ("AccountAndPersonActivity".equals(fromActivityTo)) {
							if (payType == 0) {
								if (Double.valueOf(balance) > 0 && Double.valueOf(balance) > Double.valueOf(fee)) {
									// 大象钱包支付
									returnPay(AccountAndPersonActivity.bikeid + "", userPreference.getU_tel(), "0",
											"大象钱包", 0);
								} else {
									// ToastTool.showLong(AccountSubmitActivity.this,
									// "余额不足，请先充值！");
									final CustomPopDialog cp = new CustomPopDialog.Builder(AccountSubmitActivity.this)
											.create(8);
									cp.show();
									TextView confirmTv = (TextView) CustomPopDialog.getLayout()
											.findViewById(R.id.dialog_comfirm); // 确认退出登录
									TextView dialogCancel = (TextView) CustomPopDialog.getLayout()
											.findViewById(R.id.dialog_cancel); // 取消退出登录
									confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
										@Override
										public void onClick(View v) {
											AccountSubmitActivity.this.startActivity(new Intent(
													AccountSubmitActivity.this, WalletRechargeActivity.class));
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
								// ToastTool.showLong(this, "暂时不支持支付宝支付！");
								getOrderInfo(userPreference.getU_tel(), "大象单车付款", "用车消费", fee,
										orderPreference.getBike_ID());
							}
						} else {
							// 非正常还车（直接卸载或者登录后的情况）
							getBikeidandpay(userPreference.getU_tel());
						}
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.e("获取余额失败！");
						}
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
		cLoadingDialog = new CustomProgressDialog(AccountSubmitActivity.this, "");
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.question_mark:
			final CustomPopDialog cp = new CustomPopDialog.Builder(AccountSubmitActivity.this).create(4);
			cp.show();
			TextView confirmTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm);
			TextView vipTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.GA_text_2_id);
			// Typeface tf = Typeface.createFromAsset(act.getAssets(),
			// "MyTypeface.ttc");
			confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
				}
			});
			vipTv.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
					AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, VipActivity.class));
				}
			});
			break;
		case R.id.account_person_info_btn:
			// 将其设为不可点击的状态
			personInfoBtn.setClickable(false);
			startActivity(new Intent(getApplicationContext(), PersonInfoActivity.class));
			break;
		case R.id.vip:
			this.startActivity(new Intent(this, VipActivity.class));
			break;
		case R.id.problem_submit:
			Intent intent = new Intent(AccountSubmitActivity.this, ProblemSubmitActivity.class);
			startActivity(intent);
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
			// 触发按钮的时候再去调用支付接口
			getBalance(userPreference.getU_tel());
			break;
		default:
			break;
		}
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
					Toast.makeText(AccountSubmitActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
					// 支付宝支付
					// returnPay(AccountAndPersonActivity.bikeid + "",
					// userPreference.getU_tel(), "0", "支付宝", 2);
					// 再去调用支付宝查询订单接口查询是否支付成功，以提高安全性
					queryOrderState(outTradeNo);
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(AccountSubmitActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
					} else if (TextUtils.equals(resultStatus, "4000")) {
						Toast.makeText(AccountSubmitActivity.this, "手机没有安装支付宝，请安装后付款", Toast.LENGTH_SHORT).show();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(AccountSubmitActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
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
				PayTask alipay = new PayTask(AccountSubmitActivity.this);
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
				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						String param = jsonObject.getString("param");
						String sign = jsonObject.getString("sign");
						String sign_type = jsonObject.getString("sign_type");
						outTradeNo = jsonObject.getString("out_trade_no"); // 拿到订单号,方便后期查询
						String payInfo = param + "&sign=\"" + sign + "\"&" + "sign_type=\"" + sign_type + "\"";
						LogTool.i(payInfo);

						// 向支付宝发送支付请求
						sendZfbPay(payInfo);
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {

						}
					}
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
						cpd = new CustomTipDialog.Builder(AccountSubmitActivity.this).create(0);
						cpd.show();
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								AccountSubmitActivity.this
										.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
								AccountSubmitActivity.this.finish();
							}
						}, 2000);
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.i("查询结果：订单支付成功！");
						}
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
	 * 获取金额和时长（前提是已经还车成功）
	 * 
	 * @param phone
	 */
	private void getCostandtime(String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

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
						fee = jsonObject.getString("cost");
						fee = getFee(fee);
						// 获取金额和时长成功时
						feeMuch.setText("¥" + fee);
						String time = jsonObject.getString("usedtime");
						useTimeMuch.setText(time);
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.e("获取金额和时长失败！");
						}
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
		AsyncHttpClientTool.post("api/bike/costandtime", params, responseHandler);
	}

	/**
	 * 获取单车编号和密码
	 * 
	 * @param phone
	 */
	private void getBikeidandpass(String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					String bikeid = jsonObject.getString("bikeid");
					String pass = jsonObject.getString("pass");
					if (status.equals("success")) {
						// 成功时，需要将bikeid设置保存下来中
						AS_bikeid = bikeid;
						orderPreference.setBike_ID(bikeid);
						bikeNum.setText(orderPreference.getBike_ID());
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.e("获取bikeid失败！");
						}
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
		AsyncHttpClientTool.post("api/bike/bikeidandpass2", params, responseHandler);
	}

	/**
	 * 非正常情况下，获取单车编号然后进行支付
	 * 
	 * @Title: getBikeidandpay
	 * @Description: TODO
	 * @param phone
	 * @return: void
	 */
	private void getBikeidandpay(String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					String bikeid = jsonObject.getString("bikeid");
					if (status.equals("success")) {
						if (payType == 0) {
							if (Double.valueOf(balance) > 0 && Double.valueOf(balance) > Double.valueOf(fee)) {
								// 大象钱包支付
								returnPay(bikeid + "", userPreference.getU_tel(), "0", "大象钱包", 0);
							} else {
								// ToastTool.showLong(AccountSubmitActivity.this,
								// "余额不足，请先充值！");
								final CustomPopDialog cp = new CustomPopDialog.Builder(AccountSubmitActivity.this)
										.create(8);
								cp.show();
								TextView confirmTv = (TextView) CustomPopDialog.getLayout()
										.findViewById(R.id.dialog_comfirm); // 确认退出登录
								TextView dialogCancel = (TextView) CustomPopDialog.getLayout()
										.findViewById(R.id.dialog_cancel); // 取消退出登录
								confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
									@Override
									public void onClick(View v) {
										cp.cancel();
										AccountSubmitActivity.this.startActivity(
												new Intent(AccountSubmitActivity.this, WalletRechargeActivity.class));
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
							// ToastTool.showLong(this, "暂时不支持支付宝支付！");
							// 调用支付宝
							// getOrderInfo(userPreference.getU_tel(),
							// "大象单车统一付款", "付款", "1");
							getOrderInfo(userPreference.getU_tel(), "大象单车付款", "用车消费", fee,
									orderPreference.getBike_ID());
						}
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.e("获取bikeid失败！");
						}
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
		AsyncHttpClientTool.post("api/bike/bikeidandpass2", params, responseHandler);
	}

	/**
	 * 请求生成支付订单，进行支付请求（调用微信进行支付付款）
	 * 
	 * @param phone
	 * @param totalfee
	 */
	private void wxPay(String phone, String totalfee) {

		RequestParams params = new RequestParams();
		params.put("phone", phone);
		params.put("totalfee", totalfee);
		// 从请求生成预付单开始等待
		LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// 完成后等待结束
				LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, false);
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
					// 判断是否支持微信支付
					// Toast.makeText(AccountSubmitActivity.this,
					// String.valueOf(isPaySupported), Toast.LENGTH_SHORT)
					// .show();
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

	/**
	 * 微信自带接口尝试
	 */
	private void wxPay() {
		AsyncHttpClient client = new AsyncHttpClient();

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject json = jsonTool.getJsonObject();

				if (null != json && !json.has("retcode")) {
					PayReq req = new PayReq();
					// req.appId = "wxf8b4f85f3a794e77"; // 测试用appId
					try {
						req.appId = json.getString("appid");
						req.partnerId = json.getString("partnerid");
						req.prepayId = json.getString("prepayid");
						req.nonceStr = json.getString("noncestr");
						req.timeStamp = json.getString("timestamp");
						req.packageValue = json.getString("package");
						req.sign = json.getString("sign");
						req.extData = "app data"; // optional
						Toast.makeText(AccountSubmitActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
						// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
						api.sendReq(req);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
		client.post("http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android", params, responseHandler);
	}

	/**
	 * 获取金额和时长（isnatural）
	 * 
	 * @param bikeid
	 * @param phone
	 * @param isfinish
	 */
	private void getMoneyAndTime(String bikeid, String phone, String isfinish, String isnatural) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("isfinish", isfinish);
		params.put("isnatural", isnatural);

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
						String fee = jsonObject.getString("fee");
						String time = jsonObject.getString("time");
						// 获取金额和时长成功时
						fee = getFee(fee);
						feeMuch.setText("￥" + fee);
						useTimeMuch.setText(time);
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
							getApplicationContext().startActivity(intent);
							((Activity) getApplicationContext()).finish();
						} else {
							LogTool.e("获取金额和时长失败！");
							// 目测是接口有待有修改，居然拿不到时间费用
							getCostandtime(userPreference.getU_tel());
						}
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
		AsyncHttpClientTool.post("api/money/bikefee", params, responseHandler);
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

		// 支付等待效果加载ing
		LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, true);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);

				String status = jsonTool.getStatus();
				if (status.equals("success")) {
					// 支付等待框结束
					LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, false);
					if (payModeType == 0) {// 如果是大象钱包支付
						// 支付成功时的对话框提示
						cpd = new CustomTipDialog.Builder(AccountSubmitActivity.this).create(0);
						cpd.show();
						isPaySuccess = 1;
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								goToOtherActivity();
							}
						}, 2000);
					} else if (payModeType == 1) {
						// 如果是微信支付
						// wxOrderQuery(userPreference.getU_tel());
						// 支付成功时的对话框提示
						cpd = new CustomTipDialog.Builder(AccountSubmitActivity.this).create(0);
						cpd.show();
						isPaySuccess = 1;
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								goToOtherActivity();
							}
						}, 2000);
						isPaySuccess = 1;
					} else if (payModeType == 2) {
						// 如果是支付宝支付
						// 去查询支付宝是否有支付成功的记录
						// wxOrderQuery(userPreference.getU_tel());
						// 支付成功时的对话框提示
						cpd = new CustomTipDialog.Builder(AccountSubmitActivity.this).create(0);
						cpd.show();
						isPaySuccess = 1;
						// 如果成功，延迟两秒退出
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								cpd.dismiss();
								goToOtherActivity();
							}
						}, 2000);
					}
				} else {
					String message = jsonTool.getMessage();
					if ("invalid token".equals(message)) {
						userPreference.clearAll();
						sharePreference.clearAll();
						orderPreference.clearAll();
						Intent intent = new Intent();
						AppManager.getInstance().killAllActivityExceptThis();
						intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
						getApplicationContext().startActivity(intent);
						((Activity) getApplicationContext()).finish();
					} else {
						LogTool.e("还车支付费用失败！" + message);
					}
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
	 * 跳转到其他的Activity
	 * 
	 * @Title: goToOtherActivity
	 * @Description: TODO
	 * @return: void
	 */
	private void goToOtherActivity() {
		// 如果支付成功时
		if (isPaySuccess == 1) {
			String fromActivityTo = getIntent().getStringExtra("fromActivityTo");
			// 从遇到问题，无法开锁进入时
			if ("ProblemUnableLockActivity".equals(fromActivityTo)) {
				AppManager.getInstance().killAllActivityNotCurr();
				AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
				AccountSubmitActivity.this.finish();
			} else if ("BikeLostActivity".equals(fromActivityTo)) {
				AppManager.getInstance().killAllActivityNotCurr();
				AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
				AccountSubmitActivity.this.finish();
			} else if ("ProblemPasswordActivity".equals(fromActivityTo)) {
				AppManager.getInstance().killAllActivityNotCurr();
				AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
				AccountSubmitActivity.this.finish();
			} else if ("ProblemOtherActivity".equals(fromActivityTo)) {
				AccountSubmitActivity.this.finish();
				// 逐个杀死activity
				// AppManager.getInstance().killActivity(ProblemUnableLockActivity.class);
				// AppManager.getInstance().killActivity(ProblemNextActivity.class);
			} else {
				AccountSubmitActivity.this.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
				AccountSubmitActivity.this.finish();
			}
		}
	}

	/**
	 * （微信）查询订单的支付情况
	 * 
	 * @Title: wxOrderQuery
	 * @Description: TODO
	 * @param phone
	 * @return: void
	 */
	private void wxOrderQuery(String phone) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);

				String status = jsonTool.getStatus();
				if (status.equals("success")) {
					// 查询到成功后
					LoadingPopTool.popLoadingStyle(AccountSubmitActivity.this, false);
					cpd = new CustomTipDialog.Builder(AccountSubmitActivity.this).create(0);
					cpd.show();
					// 如果成功，延迟两秒退出
					new Handler().postDelayed(new Runnable() {
						public void run() {
							// execute the task
							cpd.dismiss();
							AccountSubmitActivity.this
									.startActivity(new Intent(AccountSubmitActivity.this, CaptureActivity.class));
							AccountSubmitActivity.this.finish();
						}
					}, 2000);
				} else {
					String message = jsonTool.getMessage();
					if ("invalid token".equals(message)) {
						userPreference.clearAll();
						sharePreference.clearAll();
						orderPreference.clearAll();
						Intent intent = new Intent();
						AppManager.getInstance().killAllActivityExceptThis();
						intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
						getApplicationContext().startActivity(intent);
						((Activity) getApplicationContext()).finish();
					} else {
						LogTool.e("查询支付情况失败！" + message);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// 及时失败仍假装支付成功（因为暂时不可能回调成功）
				returnPay(AccountAndPersonActivity.bikeid + "", userPreference.getU_tel(), "0", "微信支付", 1);
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
		AsyncHttpClientTool.post("api/money/wxorderquery", params, responseHandler);
	}

	private String getFee(String str) {
		String feeString = "";
		int index = str.indexOf(".");
		int length = str.length();
		if (length - index < 3) {
			for (int i = 0; i < index + 2; i++) {
				feeString += str.charAt(i);
			}
			feeString += "0";
		} else {
			for (int i = 0; i < index + 3; i++) {
				feeString += str.charAt(i);
			}
		}
		return feeString;
	}

}
