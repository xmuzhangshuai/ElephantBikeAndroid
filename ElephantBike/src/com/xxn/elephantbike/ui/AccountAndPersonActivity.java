package com.xxn.elephantbike.ui;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.customewidget.AutoScrollTextView;
import com.xxn.elephantbike.customewidget.CustomDialog;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.ui.NoticeMessActivity.CustomListAdapter;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LocationService;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountAndPersonActivity extends BaseActivity implements OnClickListener, Runnable {

	private TextView problemMeet;// 遇到问题
	private com.xxn.elephantbike.customewidget.AutoScrollTextView textview_0, textview_1, textview_2, textview_3,
	textview_4;
	private Button replyBikeCheckout;// 还车结账
	private Button renewBike;// 恢复单车
	private ImageView vipLogo;// ImageView的logo

	private TextView bikeNum; // 单车编号
	private ImageView personInfoImage;
	private View questionMark;// 计价规则
	private TextView feeMuch;// 费用总计
	private TextView useTimeMuch;// 使用时长
	private View personInfoBtn;
	private static int REPLY_BIKE_CHECKOUT = 0;
	private static int RENEW_BIKE = 1;
	private int recLen = 0;
	private int flagTime = 0;
	private int timeFlag = 0;

	// 定位相关变量
	private LocationManager locationManager;
	private String locationProvider;

	public static String bikeid = "000000";
	public static boolean isContinue = true;
	private int exitTimeFlag = 0;
	private LocationService locationService;
	private SharePreference sharePreference;
	public static UserPreference userPreference;
	private OrderPreference orderPreference;
	private ImageView accountAdImg; // 中部广告区域
	private TextView account_flag, bike_num_text, timelong_title, fee_title, unlock_psw_text, unlock_tip, tip_help;
	static Handler messageHandler;
	Timer timer;//计时器

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_account_person);
		userPreference = BaseApplication.getInstance().getUserPreference();
		sharePreference = BaseApplication.getInstance().getSharePreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		// 初始化时把isContinue置为true
		isContinue = true;
		findViewById();
		setTypeface();
		initView();

		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, AccountAndPersonActivity.this);
		new Thread(this).start();
		// 开始加载地图配置信息
		// -----------location config ------------
		locationService = ((BaseApplication) getApplication()).locationService;
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
	}

	/**
	 * @Title: setFont
	 * @Description: 设置字体
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		account_flag.setTypeface(tf);
		bike_num_text.setTypeface(tf);
		bikeNum.setTypeface(tf);
		timelong_title.setTypeface(tf);
		useTimeMuch.setTypeface(tf);
		fee_title.setTypeface(tf);
		feeMuch.setTypeface(tf);
		unlock_psw_text.setTypeface(tf);
		textview_0.setTypeface(tf);
		textview_1.setTypeface(tf);
		textview_2.setTypeface(tf);
		textview_3.setTypeface(tf);
		textview_4.setTypeface(tf);
		unlock_tip.setTypeface(tf);
		problemMeet.setTypeface(tf);
		tip_help.setTypeface(tf);
		replyBikeCheckout.setTypeface(tf);
		renewBike.setTypeface(tf);
	}

	/*
	 * // 屏蔽返回按键
	 * 
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { return true; } return
	 * super.onKeyDown(keyCode, event); }
	 */

	@Override
	protected void findViewById() {
		textview_0 = (AutoScrollTextView) findViewById(R.id.password_unlock_0);
		textview_1 = (AutoScrollTextView) findViewById(R.id.password_unlock_1);
		textview_2 = (AutoScrollTextView) findViewById(R.id.password_unlock_2);
		textview_3 = (AutoScrollTextView) findViewById(R.id.password_unlock_3);
		textview_4 = (AutoScrollTextView) findViewById(R.id.password_unlock_4);
		vipLogo = (ImageView) findViewById(R.id.vip_logo);
		bikeNum = (TextView) findViewById(R.id.bike_num);
		questionMark = (View) findViewById(R.id.question_mark);
		problemMeet = (TextView) findViewById(R.id.problem_meet);
		replyBikeCheckout = (Button) findViewById(R.id.reply_bike_checkout);
		renewBike = (Button) findViewById(R.id.renew_bike);
		feeMuch = (TextView) findViewById(R.id.fee_much);
		useTimeMuch = (TextView) findViewById(R.id.use_time_much);
		personInfoBtn = (View) this.findViewById(R.id.account_person_info_btn);
		accountAdImg = (ImageView) this.findViewById(R.id.account_ad_img);
		account_flag = (TextView) findViewById(R.id.account_flag);
		bike_num_text = (TextView) findViewById(R.id.bike_num_text);
		timelong_title = (TextView) findViewById(R.id.timelong_title);
		fee_title = (TextView) findViewById(R.id.fee_title);
		unlock_psw_text = (TextView) findViewById(R.id.unlock_psw_text);
		unlock_tip = (TextView) findViewById(R.id.unlock_tip);
		tip_help = (TextView) findViewById(R.id.tip_help);
		personInfoImage = (ImageView) findViewById(R.id.person_info_image);
	}

	@Override
	protected void initView() {
		// messageHandler = new Handler() {
		// @Override
		// public void handleMessage(Message msg) {
		// super.handleMessage(msg);
		// if(msg.what == 0x001){
		// }
		//
		// }
		// };
		problemMeet.setText(Html.fromHtml("<u>遇到问题？</u>"));
		questionMark.setOnClickListener(this);
		problemMeet.setOnClickListener(this);
		replyBikeCheckout.setOnClickListener(this);
		renewBike.setOnClickListener(this);
		personInfoBtn.setOnClickListener(this);

		String fromActivityTo = getIntent().getStringExtra("fromActivityTo");
		if ("GuideActivity".equals(fromActivityTo) || "LoginOrRegisterActivity".equals(fromActivityTo)
				|| "ProblemOtherActivity".equals(fromActivityTo)) {
			exitTimeFlag = 1; // 异常情况进来后处理计时的处理方式
			// 还未结束还车就已经退出了
			getBikeidandlock(userPreference.getU_tel());
			// 去拉取单车编号
			getBikeidandpass(userPreference.getU_tel());
		}
		// 从扫描页正常流程过来
		// if ("CaptureActivity".equals(fromActivityTo) ||
		// "LoginOrRegisterActivity".equals(fromActivityTo)) {
		if ("CaptureActivity".equals(fromActivityTo)) {
			try {
				bikeid = getIntent().getExtras().getString("result");
				// bikeNum.setText("单车编码：" + bikeid);
				bikeNum.setText(bikeid);
				// 将单车编号存到orderPreference
				orderPreference.setBike_ID(bikeid);
				// 获取到单车编号后去调用获得解锁密码
				getUnlockcode(bikeid + "", userPreference.getU_tel());
			} catch (Exception e) {
				bikeNum.setText(bikeid);
				getUnlockcode(bikeid + "", userPreference.getU_tel());
			}

			// getNoticeMessage(userPreference.getU_tel());
		}
		// 加载主页中部广告
		onLoadAdPage();

		// 1为表示已经得到了认证，此时显示是会员LOGO，未认证不能开通大象会员
		if ("1".equals(userPreference.getU_IS_FROZEN()) || "3".equals(userPreference.getU_IS_FROZEN())
				|| "-1".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			if ("1".equals(isVip)) {
				// vipLogo.setVisibility(View.VISIBLE);
				vipLogo.setImageResource(R.drawable.vip_logo);
			}
		}
		// 当有新消息之后，个人信息图标加提示点
		if (!userPreference.getU_IS_MESSAGE().equals("0")) {
			personInfoImage.setImageResource(R.drawable.person_info_message);
		}
	}

	/*****
	 * @see copy funtion to you project
	 *      定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 * 
	 */
	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				// 设置定位位置到userPreference
				userPreference.setLatitude("" + location.getLatitude());
				userPreference.setLongitude("" + location.getLongitude());
			}
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_person_info_btn:
			personInfoBtn.setClickable(false);
			startActivity(new Intent(getApplicationContext(), PersonInfoActivity.class));
			break;
		case R.id.question_mark:
			final CustomPopDialog cp = new CustomPopDialog.Builder(AccountAndPersonActivity.this).create(4);
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
					Intent vipIntent = new Intent();
					vipIntent.putExtra("isVipFlag", userPreference.getU_is_vip()); // 这里用来传值
					vipIntent.setClass(AccountAndPersonActivity.this, VipActivity.class);
					startActivity(vipIntent);
				}
			});
			break;
		case R.id.info_money:
			this.startActivity(new Intent(this, MoneyConsumerActivity.class));
			break;
		case R.id.info_auth:
			this.startActivity(new Intent(this, AuthSubmitActivity.class));
			break;
		case R.id.share_reward:
			this.startActivity(new Intent(this, ShareRewardActivity.class));
			break;
		case R.id.info_help:
			this.startActivity(
					new Intent(this, WebActivity.class).putExtra("url", "http://www.citi-sense.cn/splmeter_help.html")
					.putExtra("title", getString(R.string.help_web)));
			this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.problem_meet:
			Intent intent = new Intent(AccountAndPersonActivity.this, ProblemNextActivity.class);
			startActivity(intent);
			break;
		case R.id.reply_bike_checkout:
			new CustomDialog.Builder(AccountAndPersonActivity.this).create(REPLY_BIKE_CHECKOUT).show();
			// 定位SDK(start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request)
			locationService.start();
			// 一旦还车付款后就立即结束掉向后台请求获取时间的操作
			// new Thread(this).stop();// 使用stop方法强行终止线程，暴力
			// isContinue = false;
			break;
		case R.id.renew_bike:
			new CustomDialog.Builder(AccountAndPersonActivity.this).create(RENEW_BIKE).show();
			break;
		default:
			break;
		}
	}

	/**
	 * 加载主页中部广告
	 * 
	 * @Title: onLoadAdPage
	 * @Description:
	 * @return: void
	 */
	private void onLoadAdPage() {
		// 如果结果中含有链接图片并且成功，则加载并显示
		if ("success".equals(sharePreference.getActivity_status_1()) && !sharePreference.getImage_url_1().isEmpty()) {
			imageLoader.displayImage(sharePreference.getImage_url_1(), accountAdImg, NoticeActivity.getImageOptions());
			// 同时集成好该图片的链接地址
			accountAdImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(AccountAndPersonActivity.this, WebActivity.class)
					.putExtra("url", sharePreference.getLink_url_1()) // 将链接地址设置到图片的URL上
					.putExtra("title", getString(R.string.activity_web)));
					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				}
			});
		}
	}

	public static DisplayImageOptions getImageOptions() {
		DisplayImageOptions options = null;
		// 使用DisplayImageOptions.Builder()创建DisplayImageOptions
		options = new DisplayImageOptions.Builder()
		// .showImageOnLoading(R.drawable.idcard_bg)// 设置图片下载期间显示的图片
		// .showImageForEmptyUri(R.drawable.idcard_bg) //
		// 设置图片Uri为空或是错误的时候显示的图片
		// .showImageOnFail(R.drawable.idcard_bg) //
		// 设置图片加载或解码过程中发生错误显示的图片
		.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
		.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
		// .displayer(new CircleBitmapDisplayer()) // 设置成圆角图片
		.build(); // 创建配置过得DisplayImageOption对象
		return options;
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
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						String bike_id = jsonObject.getString("bikeid");
						// 将单车编号存到orderPreference
						orderPreference.setBike_ID(bike_id);
						String pass = jsonObject.getString("pass");
						if (status.equals("success")) {
							// 成功时，需要将bikeid设置保存下来中
							bikeid = bike_id;
							getBikefee(bike_id + "", userPreference.getU_tel(), "0");
						} else {
							LogTool.e("获取bikeid失败！");
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

						}
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
		AsyncHttpClientTool.post("api/bike/bikeidandpass2", params, responseHandler);
	}

	/**
	 * 根据手机号来获取费用总计、使用时长、单车编号、解锁密码
	 */
	private void getBikeidandlock(String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				super.onStart();
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(AccountAndPersonActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// 等待加载完成
				LoadingPopTool.popLoadingStyle(AccountAndPersonActivity.this, false);
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				String status = jsonTool.getStatus();
				try {
					// 如果获取成功时，进行以下设值操作
					if (status.equals("success")) {
						// 单车编号
						bikeid = jsonObject.getString("bikeid");
						bikeNum.setText(bikeid);

						String psw = jsonObject.getString("pass");
						int pswNum = Integer.valueOf(psw);
						// 取出个、十、百、千
						int units, tens, hundreds, thousands, tenThousands;
						units = pswNum % 10;
						tens = (pswNum / 10) % 10;
						hundreds = (pswNum / 100) % 10;
						thousands = (pswNum / 1000) % 10;
						tenThousands = (pswNum / 10000) % 10;

						textview_0.runWithAnimation(Integer.valueOf(textview_0.getText().toString()), tenThousands);
						textview_1.runWithAnimation(Integer.valueOf(textview_1.getText().toString()), thousands);
						textview_2.runWithAnimation(Integer.valueOf(textview_2.getText().toString()), hundreds);
						textview_3.runWithAnimation(Integer.valueOf(textview_3.getText().toString()), tens);
						textview_4.runWithAnimation(Integer.valueOf(textview_4.getText().toString()), units);
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
		AsyncHttpClientTool.post("api/bike/bikeidandpass2", params, responseHandler);
	}

	/**
	 * 获取解锁密码
	 * 
	 * @param bikeid
	 *            单车编号
	 * @param phone
	 *            手机号
	 */
	private void getUnlockcode(final String bikeid, String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		// 加载进度条
		LoadingPopTool.popLoadingStyle(AccountAndPersonActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "===" + response);
				try {
					JsonTool jsonTool = new JsonTool(response);
					JSONObject jsonObject = jsonTool.getJsonObject();

					String status = jsonTool.getStatus();
					// String message = jsonTool.getMessage();
					if (status.equals("success")) {
						// 正常情况下的获取费用接口
						getBikefee(bikeid + "", userPreference.getU_tel(), "0");
						String psw = jsonObject.getString("pass");
						int pswNum = Integer.valueOf(psw);
						// 取出个、十、百、千、万
						int units, tens, hundreds, thousands, tenThousands;
						units = pswNum % 10;
						tens = (pswNum / 10) % 10;
						hundreds = (pswNum / 100) % 10;
						thousands = (pswNum / 1000) % 10;
						tenThousands = (pswNum / 10000) % 10;

						textview_0.runWithAnimation(Integer.valueOf(textview_0.getText().toString()), tenThousands);
						textview_1.runWithAnimation(Integer.valueOf(textview_1.getText().toString()), thousands);
						textview_2.runWithAnimation(Integer.valueOf(textview_2.getText().toString()), hundreds);
						textview_3.runWithAnimation(Integer.valueOf(textview_3.getText().toString()), tens);
						textview_4.runWithAnimation(Integer.valueOf(textview_4.getText().toString()), units);
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
		AsyncHttpClientTool.post("api/pass/unlockcode2", params, responseHandler);
	}

	@Override
	public void run() {
		// LogTool.e("isContinue:" + isContinue);
		while (isContinue) {
			try {
				// 每分钟发一次请求
				Thread.sleep(1000 * 60);
				
				LogTool.e("this is isContinue:" + isContinue);
				handler.sendMessage(handler.obtainMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
//			ToastTool.showLong(getApplicationContext(), isContinue+"");
			getBikefee(bikeid + "", userPreference.getU_tel(), "0");
		}
	};

	/**
	 * 获取单车费用，到这个界面的都属于未完成的订单信息，因此isfinish均为0
	 * 
	 * @Title: getBikefee
	 * @Description:
	 * @param bikeid
	 * @param phone
	 * @param isfinish
	 * @return: void
	 */
	private void getBikefee(String bikeid, String phone, String isfinish) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("isfinish", isfinish);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// 加载完成，进度结束
				LoadingPopTool.popLoadingStyle(AccountAndPersonActivity.this, false);
				LogTool.i(statusCode + "===" + response);
				try {
					JsonTool jsonTool = new JsonTool(response);
					JSONObject jsonObject = jsonTool.getJsonObject();

					String status = jsonTool.getStatus();
					// String message = jsonTool.getMessage();
					if (status.equals("success")) {
						String fee = jsonObject.getString("fee");
						fee = getFee(fee);
						String time = jsonObject.getString("time");
						// 单车费用和时间的回显
						feeMuch.setText("¥" + fee);
						useTimeMuch.setText(time);
						// 每分钟发一次请求，每次发一次请求时，当前时间记为0
						if (timeFlag == 0 && exitTimeFlag == 0) {
							timer = new Timer(true);
							timer.schedule(task, 1000, 1000); // 延时1000ms后执行，1000ms执行一次
							timeFlag = 1;// 启动一次计时线程后即停止
							// timer.cancel(); //退出计时器
						} else {
							// 如果是非正常登录
							if (exitTimeFlag == 1) {
								// 每次请求完获得时间后，取得分钟数后面的数字
								recLen = Integer.valueOf(time.substring(time.lastIndexOf(":") + 1, time.length()));
								timer = new Timer(true);
								timer.schedule(task, 1000, 1000); // 延时1000ms后执行，1000ms执行一次
								timeFlag = 1;// 启动一次计时线程后即停止
								exitTimeFlag = 0;
							}
							// 如果是正常登录
							if (exitTimeFlag == 0) {
								// 每次请求完获得时间后，取得分钟数后面的数字
								recLen = Integer.valueOf(time.substring(time.lastIndexOf(":") + 1, time.length()));
							}
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
							LogTool.e("查找不到该未完成订单！");
						}
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
		AsyncHttpClientTool.post("api/money/bikefee", params, responseHandler);
	}

	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = 1;
			handlerTime.sendMessage(message);
		}
	};

	final Handler handlerTime = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				recLen++;
				String s = (String) useTimeMuch.getText();
				String str = s.substring(0, s.lastIndexOf(":") + 1);
				if (recLen >= 60) {
					str = getTime(s);
					recLen = 0;
				}
				if(recLen < 10){
					str = str + "0";
				}
				useTimeMuch.setText(str + recLen);
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 获取使用时长的前部分的String
	 * @Title: getTime 
	 * @return: String
	 */
	private String getTime(String s) {
		int day = Integer.valueOf(s.substring(0, 2));
		int hour = Integer.valueOf(s.substring(3, 5));
		int minute = Integer.valueOf(s.substring(6, 8));
		minute ++;
		if(minute >= 60){
			minute = 0;
			hour ++;
			if(hour >= 24){
				hour = 0;
				day ++;
			}
		}
		String dayStr = day + "";
		String hourStr = hour + "";
		String minuteStr = minute + "";
		if(day < 10){ dayStr = "0" + dayStr;}
		if(hour < 10){ hourStr = "0" + hourStr;}
		if(minute < 10){ minuteStr = "0" + minuteStr;}
		return dayStr + ":" + hourStr + ":" + minuteStr + ":";
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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (userPreference.getU_IS_MESSAGE().equals("0")) {
			personInfoImage.setImageResource(R.drawable.person_info);
		}
		if (userPreference.getU_is_vip().equals("1")) {
			vipLogo.setImageResource(R.drawable.vip_logo);
		}
		// 将其设为可点击的状态
		personInfoBtn.setClickable(true);
		isContinue = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isContinue = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(timer != null){
			timer.cancel();
		}
	}

}
