package com.xxn.elephantbike.ui;

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
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/** 
 * 新通知页面
 * @ClassName: NoticeMessActivity 
 * @Description: 
 * @author: lzjing
 * @date: 2016年4月5日 上午1:38:21
 */
public class NoticeMessActivity extends BaseActivity  implements OnClickListener {

	private View navLeftBtn; // 后退按钮
	private ListView listView;
	private TextView navText;
	Intent intent;
	private UserPreference userPreference;
	String content,createtime,title;
	String[] data,dataContent,dataCreatetime,dataTitle;
	CustomListAdapter adapter;
	Boolean first = true;
	Typeface tf;
	//	private List list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_notice_mess);
		userPreference = BaseApplication.getInstance().getUserPreference();
		findViewById();
		
		initView();
		// 设置字体
		//		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		//		FontManager.changeFonts(root, NoticeMessActivity.this);
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		listView = (ListView) findViewById(R.id.list_view);
		navText = (TextView) findViewById(R.id.nav_text);
		tf = Typeface.createFromAsset(NoticeMessActivity.this.getAssets(), "MyTypeface.ttc");
		navText.setTypeface(tf);
	}

	@Override
	protected void initView() {
		navLeftBtn.setOnClickListener(this);
		getNoticeMessage(userPreference.getU_tel());

	}

	/** 
	 * @Title: getNoticeMessage 
	 * @Description: 获取活动消息
	 * @return: void
	 */
	private void getNoticeMessage(String phone) {
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
						JSONArray message = jsonObject.getJSONArray("data");
						int length = message.length();
						dataTitle = new String[length]; 
						data = new String[length];
						dataContent = new String[length];
						dataCreatetime = new String[length];
						for (int i = 0; i < length; i++) {
							dataTitle[i] = message.getJSONObject(i).getString("title");
							dataContent[i] = message.getJSONObject(i).getString("content");
							dataCreatetime[i] = message.getJSONObject(i).getString("createtime");
						}
						adapter = new CustomListAdapter(NoticeMessActivity.this);
						listView.setAdapter(adapter);
						//设置当前消息已经见过了
						userPreference.setU_IS_MESSAGE("0");
					} else {
						LogTool.e("获取消息失败！");
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
					}
				}
			}
		};
		AsyncHttpClientTool.post("api/user/message", params, responseHandler);

	}

	class CustomListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mContext = null;

		public CustomListAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		public Object getItem(int position) {
			//			return list.get(position);
			return null;
		}



		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return dataContent.length;
		}

		public View getView(int position, View convertView, android.view.ViewGroup parent) {
			if (convertView == null) {
				// 和item_custom.xml脚本关联
				convertView = mInflater.inflate(R.layout.notice_item_custom, null);
			}
			TextView mTime = (TextView) convertView.findViewById(R.id.message_time);
			TextView mTitle = (TextView) convertView.findViewById(R.id.message_title);
			TextView mDate = (TextView) convertView.findViewById(R.id.message_date);
			TextView mContent = (TextView) convertView.findViewById(R.id.message_content);
			if(first){
				mTime.setTypeface(tf);
				mTitle.setTypeface(tf);
				mDate.setTypeface(tf);
				mContent.setTypeface(tf);
			}
			String time = getTime(dataCreatetime[position]);
			String date = getDate(dataCreatetime[position]);
			mTime.setText(time);
			mTitle.setText(dataTitle[position]);
			mDate.setText(date);
			mContent.setText(dataContent[position]);
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		default:
			break;
		}
	}
	
	/** 
	 * @Title: getTime 
	 * @Description: 得到消息的时间
	 * @param string
	 * @return
	 * @return: String
	 */
	private String getTime(String time) {
		String month = time.substring(5, 7);
		String day = time.substring(8, 10);
		int hour = Integer.valueOf(time.substring(11, 13));
		String minute = time.substring(14, 16);
		if(hour < 12){
			time = month + "月" + day +"日 " + "上午 " +  hour + ":" + minute;
		}else{
			time = month + "月" + day +"日 " + "下午 " +  hour + ":" + minute;
		}
		return time;
	}
	
	/** 
	 * @Title: getDate 
	 * @Description: 得到消息的日期
	 * @param string
	 * @return
	 * @return: String
	 */
	private String getDate(String date) {
		date = date.substring(0, 10);
		return date;
	}

	

}