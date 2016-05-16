package com.xxn.elephantbike.ui;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
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
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class BikeLostActivity extends BaseActivity implements OnClickListener {

	private View navLeftBtn; // 后退按钮
	private Button sureLost;
	private UserPreference userPreference;
	private OrderPreference orderPreference;
	private TextView nav_text, problem_type, problem_type_text, amount_paid_text, amount_paid, other_desc,
			other_desc_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bike_lost);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		findViewById();
		setTypeface();
		initView();

		// // 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, BikeLostActivity.this);
	}

	/**
	 * @Title: setTypeface
	 * @Description:
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		sureLost.setTypeface(tf);
		problem_type.setTypeface(tf);
		problem_type_text.setTypeface(tf);
		amount_paid_text.setTypeface(tf);
		amount_paid.setTypeface(tf);
		other_desc.setTypeface(tf);
		other_desc_text.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		sureLost = (Button) findViewById(R.id.sure_lost);
		nav_text = (TextView) findViewById(R.id.nav_text);
		problem_type = (TextView) findViewById(R.id.problem_type);
		problem_type_text = (TextView) findViewById(R.id.problem_type_text);
		amount_paid_text = (TextView) findViewById(R.id.amount_paid_text);
		amount_paid = (TextView) findViewById(R.id.amount_paid);
		other_desc = (TextView) findViewById(R.id.other_desc);
		other_desc_text = (TextView) findViewById(R.id.other_desc_text);
	}

	@Override
	protected void initView() {
		navLeftBtn.setOnClickListener(this);
		sureLost.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			this.finish();
			break;
		case R.id.sure_lost:
			final CustomPopDialog cp = new CustomPopDialog.Builder(BikeLostActivity.this).create(9);
			cp.show();
			TextView confirmTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm); // 确认退出登录
			TextView dialogCancel = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_cancel); // 取消退出登录
			confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cp.cancel();
					upload_ques();
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
	 * @Title: upload_ques
	 * @Description: 上传问题
	 * @return: void
	 */
	private void upload_ques() {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		String bikeid = orderPreference.getBike_ID();
		String phone = userPreference.getU_tel();
		String type = getIntent().getStringExtra("type");
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("type", type);
		params.put("voiceurl", "");
		params.put("needfrozen", "1");
		params.put("needfrozen", "0");

		LoadingPopTool.popLoadingStyle(BikeLostActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LoadingPopTool.popLoadingStyle(BikeLostActivity.this, false);
				LogTool.i(statusCode + "上传成功" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				try {
					if (jsonTool.getStatus().equals("success")) {
						String message = jsonObject.getString("message");
						ToastTool.showLong(getApplicationContext(), message);
						AppManager.getInstance().killAllActivityNotCurr();
						Intent intent = new Intent(BikeLostActivity.this, PayForBikeLostActivity.class);
						intent.putExtra("fromActivityTo", "BikeLostActivity");
						startActivity(intent);
						BikeLostActivity.this.finish();
						// bikefeeMissBApi(orderPreference.getBike_ID(),
						// userPreference.getU_tel());
					} else {
						ToastTool.showLong(getApplicationContext(), "问题提交失败！");
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
		AsyncHttpClientTool.post("api/question/ques", params, responseHandler);
	}

	/**
	 * 单车丢失当isfinish为1时必须传 1 正常操作还车成功 0 遇到问题--强制还车
	 * 
	 * @Title: bikefeeMissBApi
	 * @Description: TODO
	 * @param bikeid
	 * @param phone
	 * @return: void
	 */
	private void bikefeeMissBApi(String bikeid, String phone) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("isfinish", "1");
		params.put("isnatural", "0");
		params.put("access_token", userPreference.getAccess_token());

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				JsonTool jsonTool = new JsonTool(response);
				if (jsonTool.getStatus().equals("success")) {
					ToastTool.showLong(getApplicationContext(), "确认丢失单车成功！");
				} else {
					ToastTool.showLong(getApplicationContext(), "确认丢失单车失败！");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable) {
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						ToastTool.showLong(getApplicationContext(), "服务器错误！");
					}
				}
			}

		};

		AsyncHttpClientTool.post("api/money/bikefee", null, responseHandler);
	}

}
