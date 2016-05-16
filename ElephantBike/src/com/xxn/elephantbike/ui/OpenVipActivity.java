/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: OpenVipActivity.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.ui 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年3月31日 下午2:26:48 
 * @version: V1.0   
 */
package com.xxn.elephantbike.ui;

import java.util.Calendar;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/** 
 * @ClassName: OpenVipActivity 
 * @Description: 会员开通/续费界面
 * @author: Administrator
 * @date: 2016年3月31日 下午2:26:48  
 */
public class OpenVipActivity extends BaseActivity implements OnClickListener{

	private View navLeftBtn;//后退按键
	private TextView navText;
	private RelativeLayout oneMonthVip;//开通一个月Vip
	private RelativeLayout threeMonthVip;//开通三个月Vip
	private RelativeLayout sixMonthVip;//开通六月Vip
	private RelativeLayout twelveMonthVip;//开通十二个月Vip
	private Button openVip;//确认开通
	private TextView oneMTime,threeMTime,sixMTime,twelveMTime;//会员有效期
	private int selectMonth = 0; //要开通的等级
	private UserPreference userPreference;
	private String tip = "有效期至";
	private String time;
	private String oneT, threeT, sixT, twelveT;
	private TextView onemonth_vip,onemonth_vip_price,threemonth_vip,threemonth_vip_price
				,sixmonth_vip,sixmonth_vip_price,twelvemonth_vip,twelvemonth_vip_price;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_open_vip);
		userPreference = BaseApplication.getInstance().getUserPreference();

		findViewById();
		setTypeface();	
		initView();
		// 设置字体
//		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
//		FontManager.changeFonts(root, OpenVipActivity.this);
	}

	/** 
	 * @Title: setTypeface 
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");							
		navText.setTypeface(tf);
		onemonth_vip.setTypeface(tf);
		onemonth_vip_price.setTypeface(tf);
		oneMTime.setTypeface(tf);
		threemonth_vip.setTypeface(tf);
		threemonth_vip_price.setTypeface(tf);
		threeMTime.setTypeface(tf);
		sixmonth_vip.setTypeface(tf);
		sixmonth_vip_price.setTypeface(tf);
		sixMTime.setTypeface(tf);
		twelvemonth_vip.setTypeface(tf);
		twelvemonth_vip_price.setTypeface(tf);
		twelveMTime.setTypeface(tf);
		openVip.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		navText = (TextView) findViewById(R.id.nav_text);
		openVip = (Button) findViewById(R.id.sure_open_vip);
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		sixMTime = (TextView) findViewById(R.id.sixmonth_vip_time);
		oneMTime = (TextView) findViewById(R.id.onemonth_vip_time);
		threeMTime = (TextView) findViewById(R.id.threemonth_vip_time);
		oneMonthVip = (RelativeLayout) findViewById(R.id.one_month_vip);
		sixMonthVip = (RelativeLayout) findViewById(R.id.six_month_vip);
		twelveMTime = (TextView) findViewById(R.id.twelvemonth_vip_time);
		threeMonthVip = (RelativeLayout) findViewById(R.id.three_month_vip);
		twelveMonthVip = (RelativeLayout) findViewById(R.id.twelve_month_vip);
		onemonth_vip = (TextView) findViewById(R.id.onemonth_vip);
		threemonth_vip = (TextView) findViewById(R.id.threemonth_vip);
		sixmonth_vip = (TextView) findViewById(R.id.sixmonth_vip);
		twelvemonth_vip = (TextView) findViewById(R.id.twelvemonth_vip);
		onemonth_vip_price = (TextView) findViewById(R.id.onemonth_vip_price);
		threemonth_vip_price = (TextView) findViewById(R.id.threemonth_vip_price);
		sixmonth_vip_price = (TextView) findViewById(R.id.sixmonth_vip_price);
		twelvemonth_vip_price = (TextView) findViewById(R.id.twelvemonth_vip_price);
	}

	@Override
	protected void initView() {
		navLeftBtn.setOnClickListener(this);
		oneMonthVip.setOnClickListener(this);
		threeMonthVip.setOnClickListener(this);
		sixMonthVip.setOnClickListener(this);
		twelveMonthVip.setOnClickListener(this);
		openVip.setOnClickListener(this);

		String isVipFlag = getIntent().getStringExtra("isVipFlag");
		// 直接由引导页或者由登录注册页过来
		if ("1".equals(isVipFlag)) {
			String vipTime = userPreference.getU_VIP_DATE();
			setVipTime(vipTime);
			navText.setText("会员续费");
			openVip.setText("确认续费");
		}else{
			setUnVipTime();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.one_month_vip://选择开通一个月VIP
			selectMonth = 1;
			openVip.setEnabled(true);
			resetBackground();
			oneMonthVip.setBackgroundResource(R.drawable.shape_btn_square);
			time = oneT;
			break;
		case R.id.three_month_vip://选择开通三个月VIP
			selectMonth = 3;
			openVip.setEnabled(true);
			resetBackground();
			threeMonthVip.setBackgroundResource(R.drawable.shape_btn_square);
			time = threeT;
			break;
		case R.id.six_month_vip://选择开通六个月VIP
			selectMonth = 6;
			openVip.setEnabled(true);
			resetBackground();
			sixMonthVip.setBackgroundResource(R.drawable.shape_btn_square);
			time = sixT;
			break;
		case R.id.twelve_month_vip://选择开通十二个月VIP
			selectMonth = 12;
			openVip.setEnabled(true);
			resetBackground();
			twelveMonthVip.setBackgroundResource(R.drawable.shape_btn_square);
			time = twelveT;
			break;
		case R.id.sure_open_vip://确认开通VIP
			Intent intent = new Intent(getApplicationContext(),VipAccountActivity.class);
			intent.putExtra("month", selectMonth);
			intent.putExtra("time", time);
			startActivity(intent);

			//			openvip(userPreference.getU_tel(),selectMonth +"");
			//			//结束掉前后两个Activity
			//			this.finish();
			//			AppManager.getInstance().killActivity(VipActivity.class);
			break;
		default:
			break;
		}

	}

	/** 
	 * @Title: setBackground 
	 * @Description: 重置按键背景
	 * @return: void
	 */
	private void resetBackground() {
		oneMonthVip.setBackgroundResource(R.color.white_bg);
		threeMonthVip.setBackgroundResource(R.color.white_bg);
		sixMonthVip.setBackgroundResource(R.color.white_bg);
		twelveMonthVip.setBackgroundResource(R.color.white_bg);
	}
	/**
	 * 
	 * @Title: setVipTime 
	 * @Description: 设置VIP到期时间
	 * @param time 当前vip有效期时间
	 * @return: void
	 */
	private void setVipTime(String time){

		int year = Integer.valueOf(time.substring(0, 1)) * 1000 
				+ Integer.valueOf(time.substring(1, 2)) * 100 
				+ Integer.valueOf(time.substring(2, 3)) * 10 
				+ Integer.valueOf(time.substring(3, 4));
		int month = Integer.valueOf(time.substring(5, 6)) * 10 
				+ Integer.valueOf(time.substring(6, 7));	
		int day = Integer.valueOf(time.substring(8, 9)) * 10
				+ Integer.valueOf(time.substring(9, 10));

		String dayString = day+"";
		if(day < 10){
			dayString = "0" + day;	
		}
		twelveT = (year+1) + "-" + month + "-" + dayString;
		if(month < 10){
			twelveT = (year+1) + "-0" + month + "-" + dayString;
		}
		if(month > 11){//当月份为12月時
			oneT = (year+1) + "-0" + (month-11) + "-" + dayString;
			threeT = (year+1) + "-0" + (month-9) + "-" + dayString;
			sixT = (year+1) + "-0" + (month-6) + "-" + dayString;

		}else if(month > 9){//当月份为10、11月時
			oneT = year + "-" + (month+1) + "-" + dayString;
			threeT = (year+1) + "-0" + (month-9) + "-" + dayString;
			sixT = (year+1) + "-0" + (month-6) + "-" + dayString;

		}else if(month > 6){//当月份为7、8、9月時
			if(month == 9){
				oneT = year + "-" + (month+1) + "-" + dayString;
			}else{
				oneT = year + "-0" + (month+1) + "-" + dayString;
			}
			threeT = year + "-" + (month+3) + "-" + dayString;
			sixT = (year+1) + "-0" + (month-6) + "-" + dayString;

		}else{//当月份为1-6月時
			oneT = year + "-0" + (month+1) + "-" + dayString;
			threeT = year + "-0" + (month+3) + "-" + dayString;
			if(month > 3){
				sixT = year + "-" + (month+6) + "-" + dayString;
			}else{
				sixT = year + "-0" + (month+6) + "-" + dayString;
			}
		}
		oneT = isFebruary(oneT);
		threeT = isFebruary(threeT);
		sixT = isFebruary(sixT);
		twelveT = isFebruary(twelveT);

		oneMTime.setText(tip + oneT);
		threeMTime.setText(tip + threeT);
		sixMTime.setText(tip + sixT);
		twelveMTime.setText(tip + twelveT);
	}
	/**
	 * @Title: isFebruary 
	 * @Description: 判断是否为二月
	 * @param str
	 * @return
	 * @return: String
	 */
	private String isFebruary(String str){

		int year = Integer.valueOf(str.substring(0, 1)) * 1000 
				+ Integer.valueOf(str.substring(1, 2)) * 100 
				+ Integer.valueOf(str.substring(2, 3)) * 10
				+ Integer.valueOf(str.substring(3, 4));
		int day = Integer.valueOf(str.substring(8, 9)) * 10
				+ Integer.valueOf(str.substring(9, 10));
		if(Integer.valueOf(str.substring(5, 6)) == 0 
				&& Integer.valueOf(str.substring(6, 7)) == 2){
			if(isRunNian(year)){
				if(day > 28){
					str = year+"-02-29";
				}			
			}else{
				if(day > 27){
					str = year+"-02-28";
				}
			}
		}
		return str;		
	}
	/**
	 * @Title: isRunNian 
	 * @Description: 判断是否为闰年
	 * @param year
	 * @return
	 * @return: boolean
	 */
	private boolean isRunNian(int year){
		if(year%4==0 && year%100!=0 || year%400==0){
			return true;
		}else{
			return false;
		}
	}

	/** 
	 * @Title: setUnVipTime 
	 * @Description: 设置非会员开通会员有效期
	 * @return: void
	 */
	private void setUnVipTime() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 1);// 把日期置为一个月后的日期
		oneT = getTime(c);
		oneMTime.setText(tip + oneT);

		c.add(Calendar.MONTH, 2);// 把日期置为俩个月后的日期
		threeT = getTime(c);
		threeMTime.setText(tip + threeT);

		c.add(Calendar.MONTH, 3);// 把日期置为三个月后的日期
		sixT = getTime(c);
		sixMTime.setText(tip + sixT);

		c.add(Calendar.MONTH, 6);// 把日期置为六个月后的日期
		twelveT = getTime(c);
		twelveMTime.setText(tip + twelveT);
	}

	/** 
	 * @Title: getTime 
	 * @Description: 非会员获取当前日期
	 * @return: void
	 */
	private String getTime(Calendar c) {
		int year, month, day;
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		return year+"-"+month+"-"+day;
	}
}
