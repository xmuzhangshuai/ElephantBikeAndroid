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
import com.xxn.elephantbike.customewidget.CustomProgressDialog;
import com.xxn.elephantbike.pay.PayResult;
import com.xxn.elephantbike.ui.WalletRechargeActivity.MyReceiver;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VipAccountActivity extends BaseActivity implements OnClickListener {

	private View navLeftBtn;// 后退按键
	private ImageView wxSelectedBtn;// 微信支付
	private ImageView zfbSelectedBtn;// 支付宝支付
	private RelativeLayout weixinLayout;
	private RelativeLayout zfbLayout;
	private UserPreference userPreference;
	private Button payConfirmBtn;

	private TextView vipMonth;// 会员卡类型
	private TextView vipFee;// 会员卡金额
	private TextView vipTime;// 会员卡有效期

	private IWXAPI api;
	String balance = "0"; // 余额
	private int payType = 1;// 记录支付的方式，默认使用微信支付
	String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";
	private String time;
	private int month, fee;
	private static final int SDK_PAY_FLAG = 1;
	private TextView nav_text, open_vip_name, open_vip_fee_text, open_vip_time_text, open_vip_text, weixin_text,
			weixin_text_tip, zfb_text, zfb_text_tip, problem_tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_vip_account_submit);
		userPreference = BaseApplication.getInstance().getUserPreference();

		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(VipAccountActivity.this, null);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		// 将该app注册到微信
		api.registerApp(WeChatConfig.APP_ID);

		findViewById();
		setTypeface();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, VipAccountActivity.this);
	}

	/**
	 * @Title: setTypeface
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		open_vip_name.setTypeface(tf);
		vipMonth.setTypeface(tf);
		open_vip_fee_text.setTypeface(tf);
		vipFee.setTypeface(tf);
		open_vip_time_text.setTypeface(tf);
		vipTime.setTypeface(tf);
		open_vip_text.setTypeface(tf);
		weixin_text.setTypeface(tf);
		weixin_text_tip.setTypeface(tf);
		zfb_text.setTypeface(tf);
		zfb_text_tip.setTypeface(tf);
		payConfirmBtn.setTypeface(tf);
		problem_tip.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		vipFee = (TextView) findViewById(R.id.open_vip_fee);
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		vipTime = (TextView) findViewById(R.id.open_vip_time);
		payConfirmBtn = (Button) findViewById(R.id.pay_confirm);
		vipMonth = (TextView) findViewById(R.id.open_vip_month);
		zfbLayout = (RelativeLayout) findViewById(R.id.zfb_Layout);
		weixinLayout = (RelativeLayout) findViewById(R.id.weixin_Layout);
		wxSelectedBtn = (ImageView) findViewById(R.id.wx_selected_button);
		zfbSelectedBtn = (ImageView) findViewById(R.id.zfb_selected_button);
		nav_text = (TextView) findViewById(R.id.nav_text);
		open_vip_name = (TextView) findViewById(R.id.open_vip_name);
		open_vip_fee_text = (TextView) findViewById(R.id.open_vip_fee_text);
		open_vip_time_text = (TextView) findViewById(R.id.open_vip_time_text);
		open_vip_text = (TextView) findViewById(R.id.open_vip_text);
		weixin_text = (TextView) findViewById(R.id.weixin_text);
		weixin_text_tip = (TextView) findViewById(R.id.weixin_text_tip);
		zfb_text = (TextView) findViewById(R.id.zfb_text);
		zfb_text_tip = (TextView) findViewById(R.id.zfb_text_tip);
		problem_tip = (TextView) findViewById(R.id.problem_tip);
	}

	@Override
	protected void initView() {
		zfbLayout.setOnClickListener(this);
		navLeftBtn.setOnClickListener(this);
		weixinLayout.setOnClickListener(this);
		payConfirmBtn.setOnClickListener(this);
		time = getIntent().getStringExtra("time");
		month = getIntent().getIntExtra("month", 0);
		fee = getFee(month);
		vipFee.setText(fee + ".00元");
		vipTime.setText(time);
		vipMonth.setText("(" + month + "个月)");

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
			VipAccountActivity.this.finish();
		}

		public MyReceiver() {
			// 做初始化
			System.out.println("MyReceiver init...");
		}
	}

	/**
	 * @Title: getFee
	 * @Description: 根据月份获取金额
	 * @param month
	 * @return: void
	 */
	private int getFee(int month) {
		int price = 0;
		switch (month) {
		case 1:
			price = 3;
			break;
		case 3:
			price = 7;
			break;
		case 6:
			price = 11;
			break;
		case 12:
			price = 18;
			break;
		default:
			break;
		}
		return price;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.weixin_Layout:
			wxSelectedBtn.setBackgroundResource(R.drawable.selected_button);
			zfbSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			payType = 1;
			break;
		case R.id.zfb_Layout:
			wxSelectedBtn.setBackgroundResource(R.drawable.unselected_button);
			zfbSelectedBtn.setBackgroundResource(R.drawable.selected_button);
			payType = 2;
			break;
		case R.id.pay_confirm:
			if (payType == 1) {
				wxVipPay(userPreference.getU_tel(), month + "");
				// 直接调用开通会员
				// openvip(userPreference.getU_tel(), month + "");
				// wxPay();
			} else if (payType == 2) {
				// ToastTool.showLong(this, "暂时不支持支付宝支付！");
				getOrderInfo(userPreference.getU_tel(), "会员充值", "大象单车会员充值", month + "");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 请求生成支付订单，进行支付请求（调用微信进行支付付款）
	 * 
	 * @param phone
	 * @param totalfee
	 */
	private void wxVipPay(String phone, String month) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("month", month);
		cLoadingDialog = new CustomProgressDialog(VipAccountActivity.this, "");
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
						VipAccountActivity.this.finish();
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
		AsyncHttpClientTool.post("api/pay/wxpayvip", params, responseHandler);
	}

	/**
	 * 开通会员（会员续费）
	 * 
	 * @Title: openvip
	 * @Description: TODO
	 * @param phone
	 * @param month
	 * @return: void
	 */
	private void openvip(String phone, String month) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("month", month);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i("" + statusCode + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				String status = jsonTool.getStatus();
				if (status.equals(JsonTool.STATUS_SUCCESS)) {
					LogTool.i(jsonTool.getMessage());
					try {
						ToastTool.showLong(VipAccountActivity.this, "充值成功！");
						// 缓存以及更新用户的信息到本地
						String vipdate = jsonObject.getString("vipdate");
						userPreference.setU_VIP_DATE(vipdate);
						// userPreference设置为会员
						userPreference.setU_is_vip("1");
						VipAccountActivity.this.finish();
						// 依次关闭
						AppManager.getInstance().killActivity(OpenVipActivity.class);
						AppManager.getInstance().killActivity(VipActivity.class);
						// startActivity(new
						// Intent(VipAccountActivity.this,PersonInfoActivity.class));

					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (status.equals(JsonTool.STATUS_FAIL)) {
					LogTool.e(jsonTool.getMessage());
					ToastTool.showShort(VipAccountActivity.this, jsonTool.getMessage());
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				LogTool.e("会员开通及续费操作失败！" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(VipAccountActivity.this, "api/user/openvip", params, responseHandler);
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
		LoadingPopTool.popLoadingStyle(VipAccountActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(VipAccountActivity.this, false);
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
					openvip(userPreference.getU_tel(), month + "");
					Toast.makeText(VipAccountActivity.this, "支付成功！", Toast.LENGTH_SHORT).show();
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(VipAccountActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
					} else if (TextUtils.equals(resultStatus, "4000")) {
						Toast.makeText(VipAccountActivity.this, "未安装支付宝，请安装后付款", Toast.LENGTH_SHORT).show();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(VipAccountActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
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
				PayTask alipay = new PayTask(VipAccountActivity.this);
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
	private void wxPay(String phone, String totalfee) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("totalfee", totalfee);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
					// Toast.makeText(VipAccountActivity.this,
					// String.valueOf(isPaySupported),
					// Toast.LENGTH_SHORT).show();
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
	 * 微信自带接口尝试
	 */
	private void wxPay() {
		AsyncHttpClient client = new AsyncHttpClient();

		RequestParams params = new RequestParams();

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
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
						Toast.makeText(VipAccountActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
						// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
						api.sendReq(req);
					} catch (JSONException e) {
						e.printStackTrace();
					}
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
		client.post("http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android", params, responseHandler);
	}

}
