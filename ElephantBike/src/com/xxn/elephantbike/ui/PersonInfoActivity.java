package com.xxn.elephantbike.ui;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @ClassName: PersonInfoActivity
 * @Description: 个人信息页
 * @author: lzjing
 * @date: 2016年3月20日 下午10:16:02
 */
public class PersonInfoActivity extends BaseActivity implements OnClickListener {

	private View unvipView; // unVIP
	private View vipView; // VIP
	private TextView authText; // 是否认证
	private TextView authNameText; // 认证者的姓名
	private View infoMoneyView;// 我的钱包
	private View infoAuthView;// 身份认证
	private View infoHelpView;// 用户指南
	private ImageView topicAdImage; // 底部广告栏
	private TextView moneyMuch;// 余额
	private View noticeAction;// 活动中心
	private View appExit;// app退出
	private TextView messageTip;// 新消息提醒
	private UserPreference userPreference;
	private SharePreference sharePreference;
	private OrderPreference orderPreference;
	private View tstArea;
	private TextView person_money, person_auth, info_reward, help, quit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_person_info);
		userPreference = BaseApplication.getInstance().getUserPreference();
		sharePreference = BaseApplication.getInstance().getSharePreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();

		findViewById();
		setTypeface();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, PersonInfoActivity.this);
	}
	
	/* (non Javadoc) 
	 * @Title: onResume
	 * @Description: TODO 
	 * @see com.xxn.elephantbike.base.BaseActivity#onResume() 
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	/**
	 * @Title: setTypeface
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		authNameText.setTypeface(tf);
		authText.setTypeface(tf);
		person_money.setTypeface(tf);
		moneyMuch.setTypeface(tf);
		person_auth.setTypeface(tf);
		info_reward.setTypeface(tf);
		messageTip.setTypeface(tf);
		help.setTypeface(tf);
		quit.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		unvipView = (View) findViewById(R.id.unvip);
		vipView = (View) findViewById(R.id.vip);
		infoMoneyView = (View) findViewById(R.id.info_money);
		infoAuthView = (View) findViewById(R.id.info_auth);
		infoHelpView = (View) findViewById(R.id.info_help);
		authText = (TextView) findViewById(R.id.auth);
		authNameText = (TextView) findViewById(R.id.name);
		topicAdImage = (ImageView) findViewById(R.id.topic_ad);
		moneyMuch = (TextView) findViewById(R.id.money_remain_much);
		noticeAction = (View) findViewById(R.id.notice_action);
		tstArea = (View) findViewById(R.id.transparent_area);
		appExit = (View) findViewById(R.id.app_exit);
		messageTip = (TextView) findViewById(R.id.info_message_tip);
		person_money = (TextView) findViewById(R.id.person_money);
		person_auth = (TextView) findViewById(R.id.person_auth);
		info_reward = (TextView) findViewById(R.id.info_reward);
		help = (TextView) findViewById(R.id.help);
		quit = (TextView) findViewById(R.id.quit);

	}

	@Override
	protected void initView() {
		unvipView.setOnClickListener(this);
		vipView.setOnClickListener(this);
		infoMoneyView.setOnClickListener(this);
		infoAuthView.setOnClickListener(this);
		infoHelpView.setOnClickListener(this);
		noticeAction.setOnClickListener(this);
		tstArea.setOnClickListener(this);
		appExit.setOnClickListener(this);
		getInfoForDisplay();
		// 加载底部广告
		onLoadAdPage();
		// 获取金额
		if (!"".equals(userPreference.getU_tel()))// 当userPreference中没有该值时，返回空
			getMoneyRemainMuch(userPreference.getU_tel());
		// 获取会员有效期
		getVipdate(userPreference.getU_tel());

		// 当有新消息之后，新消息提醒可见
		if(!userPreference.getU_IS_MESSAGE().equals("0")){
			messageTip.setVisibility(View.VISIBLE);
		}
	}

	// 返回键的时间触发
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			// 不存在第二个动画效果
			overridePendingTransition(R.anim.out_to_left, R.anim.out_to_left);
			return false;
		}
		return false;
	}

	/**
	 * 用于显示个人信息（是否为会员，是否认证过）
	 * 
	 * @Title: getInfoForDisplay
	 * @Description: TODO
	 * @return
	 * @return: Boolean
	 */
	private Boolean getInfoForDisplay() {
		Boolean infoFlag = false;
		// 1为表示已经得到了认证，此时显示是否为会员，名字等信息
		if ("1".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			String college = userPreference.getU_COLLEGE();
			String name = userPreference.getU_NAME();
			if ("1".equals(isVip)) {
				unvipView.setVisibility(View.INVISIBLE);
				vipView.setVisibility(View.VISIBLE);
				// vipView.setBackgroundResource(R.drawable.shape_label_green);
			}
			authText.setText("已认证：" + college);
			authNameText.setText(name);
			infoFlag = true;
		}
		// -1为表示冻结，此时显示是否为会员，名字等信息
		if ("-1".equals(userPreference.getU_IS_FROZEN()) || "3".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			String college = userPreference.getU_COLLEGE();
			String name = userPreference.getU_NAME();
			if ("1".equals(isVip)) {
				unvipView.setVisibility(View.INVISIBLE);
				vipView.setVisibility(View.VISIBLE);
				// vipView.setBackgroundResource(R.drawable.shape_label_green);
			}
			authText.setText("已认证：" + college);
			authNameText.setText(name + "（冻结）");
			authNameText.setTextColor(Color.RED);
			infoFlag = true;
		}
		// 表示此时未认证
		if ("0".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			String name = userPreference.getU_NAME();
			if ("1".equals(isVip)) {
				unvipView.setVisibility(View.INVISIBLE);
				vipView.setVisibility(View.VISIBLE);
			}
			unvipView.setEnabled(false);
			authNameText.setText(name);
			infoFlag = true;
		}

		// 表示此时待审核状态
		if ("2".equals(userPreference.getU_IS_FROZEN())) {
			String isVip = userPreference.getU_is_vip();
			String name = userPreference.getU_NAME();
			if ("1".equals(isVip)) {
				unvipView.setVisibility(View.INVISIBLE);
				vipView.setVisibility(View.VISIBLE);
			}
			//待审核不让开通大象会员
			unvipView.setEnabled(false);
			authText.setText("待审核");
			authNameText.setText(name);
			infoFlag = true;
		}
		return infoFlag;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.transparent_area:
			this.finish();
			overridePendingTransition(R.anim.out_to_left, R.anim.out_to_left);
			break;
		case R.id.notice_action:
			// 点击之后，把新消息置为0
			userPreference.setU_IS_MESSAGE("0");
			this.finish();
			startActivity(new Intent(getApplicationContext(), NoticeMessActivity.class));
			break;
		case R.id.unvip:
			this.finish();// 先结束掉该遮盖层
			Intent intent = new Intent();
			intent.putExtra("isVipFlag", userPreference.getU_is_vip()); // 这里用来传值
			intent.setClass(PersonInfoActivity.this, VipActivity.class);
			startActivity(intent);
			break;
		case R.id.vip:
			this.finish();// 先结束掉该遮盖层
			Intent vipIntent = new Intent();
			vipIntent.putExtra("isVipFlag", userPreference.getU_is_vip()); // 这里用来传值
			vipIntent.setClass(PersonInfoActivity.this, VipActivity.class);
			startActivity(vipIntent);
			break;
		case R.id.info_money:
			this.finish();
			startActivity(new Intent(getApplicationContext(), MoneyConsumerActivity.class));
			break;
		case R.id.info_auth:
			this.finish();
			startActivity(new Intent(getApplicationContext(), AuthSubmitActivity.class));
			break;
		case R.id.info_help:
			startActivity(new Intent(PersonInfoActivity.this, WebActivity.class).putExtra("url", "http://www.baidu.com")
					.putExtra("title", getString(R.string.help_web)));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;
		case R.id.app_exit:
			final CustomPopDialog cp = new CustomPopDialog.Builder(PersonInfoActivity.this).create(6);
			cp.show();
			TextView confirmTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm); // 确认退出登录
			TextView dialogCancel = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_cancel); // 取消退出登录
			confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
					userPreference.clearAll();
					sharePreference.clearAll();
					orderPreference.clearAll();
					AppManager.getInstance().AppExit(getApplicationContext());
				}
			});
			dialogCancel.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
				}
			});
			break;
		default:
			break;
		}
	}

	/**
	 * 加载底部广告
	 * 
	 * @Title: onLoadAdPage
	 * @Description: TODO
	 * @return: void
	 */
	private void onLoadAdPage() {
		// 如果结果中含有链接图片并且成功，则加载并显示
		if ("success".equals(sharePreference.getActivity_status_2()) && !sharePreference.getImage_url_2().isEmpty()) {
			imageLoader.displayImage(sharePreference.getImage_url_2(), topicAdImage, NoticeActivity.getImageOptions());
			// 同时集成好该图片的链接地址
			topicAdImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(PersonInfoActivity.this, WebActivity.class)
					.putExtra("url", sharePreference.getLink_url_2()) // 将链接地址设置到图片的URL上
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
		.displayer(new RoundedBitmapDisplayer(5)) // 设置成圆角图片
		.build(); // 创建配置过得DisplayImageOption对象
		return options;
	}

	/**
	 * 根据手机号来获取钱包余额
	 */
	private void getMoneyRemainMuch(String phone) {

		RequestParams params = new RequestParams();
		params.put("phone", phone);
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					// 如果获取成功时，进行以下设值操作
					if (status.equals("success")) {
						// 钱包余额
						String balance = jsonObject.getString("balance");
						// 获取金额，将moneyRemainMuch设置余额
						moneyMuch.setText("余额：" + balance + "元");
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
						LogTool.e("设置金额失败！");
					}
				}
			}

		};

		AsyncHttpClientTool.post("api/money/balance", params, responseHandler);
	}

	/**
	 * 将是否为会员和会员有效期提前存入userPreference中
	 * 
	 * @Title: getMoneyRemainMuch
	 * @Description: TODO
	 * @param phone
	 * @return: void
	 */
	private void getVipdate(String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(PersonInfoActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// 等待完成
				LoadingPopTool.popLoadingStyle(PersonInfoActivity.this, false);
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					// 如果获取成功时，进行以下设值操作
					if (status.equals("success")) {
						// isvip
						String isvip = jsonObject.getString("isvip");
						// vipdate
						String vipdate = jsonObject.getString("vipdate");

						// 将是否为会员和会员有效期存入userPreference中
						userPreference.setU_is_vip(isvip);
						userPreference.setU_VIP_DATE(vipdate);
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
						LogTool.e("设置金额失败！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/user/vipdate", params, responseHandler);
	}

}
