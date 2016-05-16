package com.xxn.elephantbike.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseFragmentActivity;

public class ShareRewardActivity extends BaseFragmentActivity {

	private View navLeftBtn; //后退按钮
	private Button shareWx;//微信分享好友
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_share_reward);

		findViewById();
		initView();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		shareWx = (Button) findViewById(R.id.share_weixin);
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
		shareWx.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ShareRewardActivity.this,
						ShareMenuActivity.class);
				startActivity(intent);
			}
		});
	}

}
