package com.xxn.elephantbike.ui;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.utils.FontManager;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ProblemNextActivity extends BaseActivity  implements OnClickListener {

	private View navLeftBtn; // 后退按钮
	//以下是遇到问题页中的选项
	private TextView pbmeettv_1;
	private TextView pbmeettv_2;
	private TextView pbmeettv_3;
	private TextView pbmeettv_4;
	Intent intent;
	private Button problemNextSubmit;
	private TextView nav_text;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_problem_next);
		findViewById();
		setTypeface();	
		initView();
		// 设置字体
//		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
//		FontManager.changeFonts(root, ProblemNextActivity.this);
	}

	/** 
	 * @Title: setTypeface 
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");							
		nav_text.setTypeface(tf);
		pbmeettv_1.setTypeface(tf);
		pbmeettv_2.setTypeface(tf);
		pbmeettv_3.setTypeface(tf);
		pbmeettv_4.setTypeface(tf);
		problemNextSubmit.setTypeface(tf);
		
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		pbmeettv_1 = (TextView) findViewById(R.id.problem_meet_text1);
		pbmeettv_2 = (TextView) findViewById(R.id.problem_meet_text2);
		pbmeettv_3 = (TextView) findViewById(R.id.problem_meet_text3);
		pbmeettv_4 = (TextView) findViewById(R.id.problem_meet_text4);
		problemNextSubmit = (Button) findViewById(R.id.problem_next_submit);
		nav_text = (TextView) findViewById(R.id.nav_text);
	}

	@Override
	protected void initView() {
		pbmeettv_1.setOnClickListener(this);
		pbmeettv_2.setOnClickListener(this);
		pbmeettv_3.setOnClickListener(this);
		pbmeettv_4.setOnClickListener(this);
		navLeftBtn.setOnClickListener(this);
		problemNextSubmit.setOnClickListener(this);
	}

	/* (non Javadoc) 
	 * @Title: onClick
	 * @Description: 点击事件处理
	 * @param v 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) 
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.problem_meet_text1:
			intent = new Intent(this, ProblemUnableLockActivity.class);
			intent.putExtra("type", pbmeettv_1.getText().toString());
			resetBackground();
			pbmeettv_1.setBackgroundResource(R.drawable.shape_btn_square);
			break;
		case R.id.problem_meet_text2:
			intent = new Intent(this, BikeLostActivity.class);
			intent.putExtra("type", pbmeettv_2.getText().toString());
			resetBackground();
			pbmeettv_2.setBackgroundResource(R.drawable.shape_btn_square);
			break;
		case R.id.problem_meet_text3:
			intent = new Intent(this, ProblemPasswordActivity.class);
			intent.putExtra("type", pbmeettv_3.getText().toString());
			resetBackground();
			pbmeettv_3.setBackgroundResource(R.drawable.shape_btn_square);
			break;
		case R.id.problem_meet_text4:
			intent = new Intent(this, ProblemOtherActivity.class);
			intent.putExtra("type", pbmeettv_4.getText().toString());
			resetBackground();
			pbmeettv_4.setBackgroundResource(R.drawable.shape_btn_square);
			break;
		case R.id.problem_next_submit:
			if(intent == null){//只有选择问题之后才能点下一步
				Toast.makeText(getApplicationContext(), "请选择一个问题",
						Toast.LENGTH_LONG).show();
			}else{
				this.startActivity(intent);	
			}
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
		pbmeettv_1.setBackgroundResource(R.color.white_bg);
		pbmeettv_2.setBackgroundResource(R.color.white_bg);
		pbmeettv_3.setBackgroundResource(R.color.white_bg);
		pbmeettv_4.setBackgroundResource(R.color.white_bg);
	}

}
