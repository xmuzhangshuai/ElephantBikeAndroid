package com.xxn.elephantbike.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.base.BaseFragmentActivity;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MoneyConsumerActivity extends BaseFragmentActivity {

	private View navLeftBtn; // 后退按钮
	private UserPreference userPreference;
	private TextView moneyRemainMuch;
	private TextView instantRecharge;
	private TextView tipText;
	private ListView listView;
	private List<String> moneyTimeItems;
	private TextView nav_text, money_text, tip_text, balance_detail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_money_consumer);
		userPreference = BaseApplication.getInstance().getUserPreference();

		findViewById();
		setTypeface();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, MoneyConsumerActivity.this);
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: onRestart
	 * 
	 * @Description: TODO
	 * 
	 * @see com.xxn.elephantbike.base.BaseFragmentActivity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		// 初始化数值，去服务器获取余额
		getMoneyRemainMuch(userPreference.getU_tel());
		// 初始化数值，去服务器获取余额明细
		getBalanceList();
	}

	/**
	 * @Title: setTypeface
	 * @Description:
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		money_text.setTypeface(tf);
		moneyRemainMuch.setTypeface(tf);
		tip_text.setTypeface(tf);
		instantRecharge.setTypeface(tf);
		balance_detail.setTypeface(tf);
		tipText.setTypeface(tf);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		instantRecharge = (TextView) findViewById(R.id.instant_recharge);
		moneyRemainMuch = (TextView) findViewById(R.id.money_remain_much);
		listView = (ListView) findViewById(R.id.list_view);
		tipText = (TextView) findViewById(R.id.tip_text);
		nav_text = (TextView) findViewById(R.id.nav_text);
		money_text = (TextView) findViewById(R.id.money_text);
		tip_text = (TextView) findViewById(R.id.tip_text);
		balance_detail = (TextView) findViewById(R.id.balance_detail);
	}

	@Override
	protected void initView() {

		instantRecharge.setText(Html.fromHtml("<u>立即充值></u>"));
		navLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		instantRecharge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MoneyConsumerActivity.this, WalletRechargeActivity.class);
				startActivity(intent);
			}
		});

		// List<Model> models = new ArrayList<Model>();
		// for (int i = 0; i < 400; i++) {
		// Model model = new Model("key" + i, "2012" + i);
		// models.add(model);
		// }
		// MyAdapter1 adapter = new MyAdapter1(getApplicationContext(), models);
		// fillArray();

		// 初始化数值，去服务器获取余额
		getMoneyRemainMuch(userPreference.getU_tel());
		// 初始化数值，去服务器获取余额明细
		getBalanceList();
	}

	/**
	 * 获取余额明细
	 */
	private void getBalanceList() {
		moneyTimeItems = new ArrayList<String>();
		String phone = userPreference.getU_tel();
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		params.put("count", "0");

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(MoneyConsumerActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// 等待加载完成
				LoadingPopTool.popLoadingStyle(MoneyConsumerActivity.this, false);
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					// 如果获取成功时，进行以下设值操作
					if (status.equals("success")) {
						// 获取的data数据依旧为json对象，因此重新封装下(钱包余额明细)
						JSONArray jsonArray = jsonObject.getJSONArray("data");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObjectSon = (JSONObject) jsonArray.opt(i);
							moneyTimeItems
									.add(jsonObjectSon.getString("fee") + "/" + jsonObjectSon.getString("fee_time"));
						}
						listView.setAdapter(new CustomListAdapter(MoneyConsumerActivity.this));
					} else {
						LogTool.e("获取余额明细失败！");
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
						LogTool.e("获取余额明细失败！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/money/balancelist", params, responseHandler);
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
						moneyRemainMuch.setText(balance);
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
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/money/balance", params, responseHandler);
	}

	/**
	 * 根据手机号来获取钱包余额
	 */
	private void getBalancelist(String phone, String count) {

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
						moneyRemainMuch.setText(balance);
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
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/money/balance", params, responseHandler);
	}

	class CustomListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mContext = null;

		public CustomListAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return moneyTimeItems.get(arg0);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return moneyTimeItems.size();
		}

		public View getView(int position, View convertView, android.view.ViewGroup parent) {
			final TextView consumerType;
			final TextView handleTime, handleMoneyMuch;
			if (convertView == null) {
				// 和item_custom.xml脚本关联
				convertView = mInflater.inflate(R.layout.consumer_item_custom, null);
			}
			// 解析字符串中的信息
			String moneyTimeInfo = moneyTimeItems.get(position).toString();
			// 消费类型
			consumerType = (TextView) convertView.findViewById(R.id.money_handle_type);
			handleTime = (TextView) convertView.findViewById(R.id.handle_time);
			handleMoneyMuch = (TextView) convertView.findViewById(R.id.handle_money_much);
			String[] strArray = null;
			strArray = moneyTimeInfo.split("/");
			// 消费类型
			if (strArray[0].contains("-"))// 消费
				consumerType.setText("消费");
			else
				consumerType.setText("充值");
			// 设置item中handleTime/handleMoneyMuch的文本
			// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd
			// HH:mm:ss");
			// handleTime.setText(formatter.format(new
			// Date(System.currentTimeMillis())));
			handleTime.setText(strArray[1]);
			handleMoneyMuch.setText(strArray[0]);
			// 设置item中ImageView的图片
			// indexImage.setBackgroundResource(R.drawable.icon);
			return convertView;
		}
	}
}
