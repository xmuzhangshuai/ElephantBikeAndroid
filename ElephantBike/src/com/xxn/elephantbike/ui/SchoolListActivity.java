/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: SchoolListActivity.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.ui 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年4月1日 下午6:03:27 
 * @version: V1.0   
 */
package com.xxn.elephantbike.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

/**
 * @ClassName: SchoolListActivity
 * @Description: 显示学校列表,并返回选中的学校名字
 * @author: Administrator
 * @date: 2016年4月1日 下午6:03:27
 */
public class SchoolListActivity extends BaseActivity implements OnItemClickListener {
	private TextView schoolName;
	private UserPreference userPreference;
	ListView schooList;
	String[] data;
	MyAdapter adapter;
	String schName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_school_list);
		userPreference = BaseApplication.getInstance().getUserPreference();

		findViewById();
		initView();
		// 设置字体
		ViewGroup root = (ViewGroup) this.getWindow().getDecorView();
		FontManager.changeFonts(root, SchoolListActivity.this);
		getSchoolLIst();
	}
/*
	// 屏蔽返回按键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	*/

	@Override
	protected void findViewById() {
		schooList = (ListView) findViewById(R.id.school_list);
	}

	@Override
	protected void initView() {
		schooList.setOnItemClickListener(this);
	}

	private class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.school_list_item, null);
				schoolName = (TextView) convertView.findViewById(R.id.school_name);
				Typeface tf = Typeface.createFromAsset(getAssets(), "MyTypeface.ttc");
				schoolName.setTypeface(tf);
			}
			schoolName.setText(data[position]);
			return convertView;
		}
	}

	/**
	 * 获取学校名字显示到控件中
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = getIntent();
		TextView sName = (TextView) view.findViewById(R.id.school_name);
		schName = sName.getText().toString();
		intent.putExtra("schoolName", schName);
		setResult(4, intent);
		overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
		finish();
	}

	/**
	 * 获取学校信息列表
	 * 
	 * @Title: getSchoolLIst
	 * @Description: TODO
	 * @return: void
	 */
	private void getSchoolLIst() {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		LoadingPopTool.popLoadingStyle(SchoolListActivity.this, true);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				try {
					if (status.equals("success")) {
						JSONArray college = jsonObject.getJSONArray("college");
						int length = college.length();
						data = new String[length];
						for (int i = 0; i < college.length(); i++) {
							data[i] = college.getJSONObject(i).getString("name");
						}
						adapter = new MyAdapter();
						schooList.setAdapter(adapter);
						LoadingPopTool.popLoadingStyle(SchoolListActivity.this, false);
					} else {
						LogTool.e("获取学校信息失败！");
						ToastTool.showLong(getApplicationContext(), "可能没有学校信息！");
						data[0] = "厦门大学";
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
						LogTool.e("获取学校信息失败！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("allcollege", params, responseHandler);
	}

}
