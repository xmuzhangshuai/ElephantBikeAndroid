package com.xxn.elephantbike.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.config.Constants;
import com.xxn.elephantbike.config.Constants.WeChatConfig;

/** 
 * 类描述 ：在设置页面点击告诉小伙伴跳出的平台选项
 * 类名： ShareActivity.java  
 * Copyright:   Copyright (c)2015    
 * Company:     zhangshuai   
 * @author:     zhangshuai    
 * @version:    1.0    
 * 创建时间:    2015-8-15 上午10:27:14  
*/
public class ShareMenuActivity extends BaseActivity implements OnClickListener {
	private View weixinFriends;
	private View weixinQuan;
	// 首先在您的Activity中添加如下成员变量

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_menu);

		findViewById();
		initView();
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		weixinFriends = findViewById(R.id.menu0);
		weixinQuan = findViewById(R.id.menu1);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		weixinFriends.setOnClickListener(this);
		weixinQuan.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.menu0:

			break;
		case R.id.menu1:
			break;

		default:
			break;
		}
	}

	public void cancel(View view) {
		finish();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**使用SSO授权必须添加如下代码 */
	}

}
