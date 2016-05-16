/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: VIPActivity.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.ui 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年3月31日 上午11:26:53 
 * @version: V1.0   
 */
package com.xxn.elephantbike.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.UserPreference;

/** 
 * @ClassName: VIPActivity 
 * @Description: 大象会员信息界面
 * @author: 张勋
 * @date: 2016年3月31日 上午11:26:53  
 */
public class VipActivity extends BaseActivity implements OnClickListener{

	private View navLeftBtn;//后退按键
	private TextView vipTime;//会员到期时间 或者 非会员文本(默认非会员)
	private Button openVip; //会员续费或者开通会员(默认非会员)
	private UserPreference userPreference;
	private ImageView vipImage;
	private TextView vipPhone, navText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_vip);
		userPreference = BaseApplication.getInstance().getUserPreference();

		findViewById();
		setTypeface();	
		initView();
		// 设置字体
//		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
//		FontManager.changeFonts(root, VipActivity.this);
	}


	/** 
	 *  设置字体
	 * @Title: setTypeface 
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");							
		navText.setTypeface(tf);
		vipTime.setTypeface(tf);
		openVip.setTypeface(tf);
	}


	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		vipTime = (TextView) findViewById(R.id.vip_time);
		openVip = (Button) findViewById(R.id.open_vip);
		vipImage = (ImageView) findViewById(R.id.vip_image);
		vipPhone = (TextView) findViewById(R.id.vip_phone);
		navText = (TextView) findViewById(R.id.nav_text);
	}


	@Override
	protected void initView() {
		navLeftBtn.setOnClickListener(this);
		openVip.setOnClickListener(this);

		String isVipFlag = getIntent().getStringExtra("isVipFlag");
		// 直接由引导页或者由登录注册页过来
		if ("1".equals(isVipFlag)) {
			vipTime.setText("您的会员将于 "+ userPreference.getU_VIP_DATE() +"到期！");
//			vipPhone.setText(userPreference.getU_tel());
			openVip.setText("会员续费");
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.open_vip:
			Intent intent = new Intent();
			intent.putExtra("isVipFlag", userPreference.getU_is_vip()); // 这里用来传值
			intent.setClass(getApplicationContext(), OpenVipActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
