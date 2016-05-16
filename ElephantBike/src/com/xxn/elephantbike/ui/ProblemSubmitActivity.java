package com.xxn.elephantbike.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.customewidget.CustomTwoMenuDialog;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.LocationService;

public class ProblemSubmitActivity extends BaseActivity {

	private View navLeftBtn; // 后退按钮
	private LinearLayout problemDesc; // 问题描述
	private LinearLayout bikeLocation; // 单车位置
	private RelativeLayout uploadCertLayout; // 上传证书
	private ListView problemTypeList; // 问题列表项
	private EditText selectBikeLoc;

	private String[] problemTexts; // 问题列表名
	private int preViewPst = -1;
	private Button problemSubmit;
	private int choicePostion = -1;

	private LocationService locationService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_problem_submit);

		findViewById();
		initProblemList(); // 初始化问题列表类型
		initView();
		// 设置字体
//		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
//		FontManager.changeFonts(root, ProblemSubmitActivity.this);
		// 开始加载地图配置信息
		// -----------location config ------------
		locationService = ((BaseApplication) getApplication()).locationService;
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
	}

	// 屏蔽返回按键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// finish掉当前的提交问题页
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	private void initProblemList() {
		// TODO Auto-generated method stub
		problemTexts = new String[] { getString(R.string.bike_lost), getString(R.string.noway_checkout),
				getString(R.string.bike_damage), getString(R.string.lock_damage), getString(R.string.other_problem) };
		List contents = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < problemTexts.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("problemType", problemTexts[i]);
			contents.add(map);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, contents, R.layout.activity_problem_type_item,
				new String[] { "problemType" }, new int[] { R.id.problem_type_name });

		problemTypeList.setAdapter(adapter);
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		problemTypeList = (ListView) findViewById(R.id.problem_type_list);
		problemSubmit = (Button) findViewById(R.id.problem_submit);

		// 遇到问题页的三个可见与不可见的状态
		problemDesc = (LinearLayout) findViewById(R.id.problem_description);
		uploadCertLayout = (RelativeLayout) findViewById(R.id.upload_cert);
		bikeLocation = (LinearLayout) findViewById(R.id.bike_location);
		selectBikeLoc = (EditText) findViewById(R.id.select_bike_location);

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

		// 问题类型列表触发事件
		problemTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object lastCheckedOption = parent.getItemAtPosition(position);
				if (lastCheckedOption != null) {
					// 一定需要选择一个问题类型，其中View为适配器View
					View v = view.findViewById(R.id.problem_type_checked);
					v.setVisibility(View.VISIBLE);
					if (preViewPst != -1 && position != preViewPst) {
						parent.getChildAt(preViewPst).findViewById(R.id.problem_type_checked)
						.setVisibility(View.INVISIBLE);
					}
					// 记录前一个选择的问题类型，以便下次选择不同时，清除
					preViewPst = position;
					// 点击了就将所有的子项目显示出来
					problemDesc.setVisibility(View.VISIBLE);
					bikeLocation.setVisibility(View.VISIBLE);
					uploadCertLayout.setVisibility(View.VISIBLE);
					// 定位SDK(start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request)
					locationService.start();
					// 单车丢失时，是不需要显示单车位置的
					if (position == 0) {
						bikeLocation.setVisibility(View.INVISIBLE);
					}
				}
				//只要是点击了修改选择的标志位
				choicePostion = position;
			}
		});

		// 问题提交触发事件,响应遇到问题页面
		problemSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (choicePostion == 0) {
					// 此时需要去认证
					CustomTwoMenuDialog ctmd = new CustomTwoMenuDialog.Builder(ProblemSubmitActivity.this).create(0);
					ctmd.show();
				} else {
					Intent intent = new Intent(ProblemSubmitActivity.this, CaptureActivity.class);
					startActivity(intent);
				}
			}
		});

	}


	/*****
	 * @see copy funtion to you project
	 *      定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
	 * 
	 */
	private BDLocationListener mListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				StringBuffer sb = new StringBuffer(256);
				sb.append("time : ");
				/**
				 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
				 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
				 */
				sb.append(location.getTime());
				sb.append("\nerror code : ");
				sb.append(location.getLocType());
				// 纬度
				sb.append("\nlatitude : ");
				sb.append(location.getLatitude());
				// 经度
				sb.append("\nlontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nradius : ");
				sb.append(location.getRadius());
				sb.append("\nCountryCode : ");
				sb.append(location.getCountryCode());
				sb.append("\nCountry : ");
				sb.append(location.getCountry());
				sb.append("\ncitycode : ");
				sb.append(location.getCityCode());
				sb.append("\ncity : ");
				sb.append(location.getCity());
				sb.append("\nDistrict : ");
				sb.append(location.getDistrict());
				sb.append("\nStreet : ");
				sb.append(location.getStreet());
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\nDescribe: ");
				sb.append(location.getLocationDescribe());
				sb.append("\nDirection(not all devices have value): ");
				sb.append(location.getDirection());
				sb.append("\nPoi: ");
				if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
					for (int i = 0; i < location.getPoiList().size(); i++) {
						Poi poi = (Poi) location.getPoiList().get(i);
						sb.append(poi.getName() + ";");
					}
				}
				if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
					sb.append("\nspeed : ");
					sb.append(location.getSpeed());// 单位：km/h
					sb.append("\nsatellite : ");
					sb.append(location.getSatelliteNumber());
					sb.append("\nheight : ");
					sb.append(location.getAltitude());// 单位：米
					sb.append("\ndescribe : ");
					sb.append("gps定位成功");
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
					// 运营商信息
					sb.append("\noperationers : ");
					sb.append(location.getOperators());
					sb.append("\ndescribe : ");
					sb.append("网络定位成功");
				} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
					sb.append("\ndescribe : ");
					sb.append("离线定位成功，离线定位结果也是有效的");
				} else if (location.getLocType() == BDLocation.TypeServerError) {
					sb.append("\ndescribe : ");
					sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
					sb.append("\ndescribe : ");
					sb.append("网络不同导致定位失败，请检查网络是否通畅");
				} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
					sb.append("\ndescribe : ");
					sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				}
				// logMsg(sb.toString());
				// 设置定位位置
				selectBikeLoc.setText(location.getAddrStr());
			}
		}

	};

}
