/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: ProblemOteher.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.ui 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年3月31日 上午10:36:09 
 * @version: V1.0   
 */
package com.xxn.elephantbike.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

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
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @ClassName: ProblemOteher
 * @Description: 不影响还车的损坏问题
 * @author: 张勋
 * @date: 2016年3月31日 上午10:36:09
 */
public class ProblemOtherActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	private View navLeftBtn;// 后退按钮
	private ImageButton inputVoice;// 录制语音
	private Button submit; // 确认
	private TextView voiceTip;// 正在录音提示
	private int needRelease = 0;
	// 录音
	private MediaRecorder myRecorder;
	// 音频文件保存地址
	private String path;
	private String paths = path;
	private File saveFilePath = null;
	int count = 0;
	String url;
	int x = 0, y = 0;// 移动时的xy值
	private UserPreference userPreference;
	private OrderPreference orderPreference;
	private TextView nav_text, problem_type, problem_type_text, voice_desc, voice_desc_text, input_voice_tip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_problem_other);
		userPreference = BaseApplication.getInstance().getUserPreference();
		orderPreference = BaseApplication.getInstance().getOrderPreference();
		findViewById();
		setTypeface();
		initView();

	}

	/**
	 * @Title: setTypeface
	 * @Description:
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		problem_type.setTypeface(tf);
		problem_type_text.setTypeface(tf);
		voice_desc.setTypeface(tf);
		voice_desc_text.setTypeface(tf);
		voiceTip.setTypeface(tf);
		submit.setTypeface(tf);
		input_voice_tip.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		inputVoice = (ImageButton) findViewById(R.id.input_voice);
		submit = (Button) findViewById(R.id.submit);
		voiceTip = (TextView) findViewById(R.id.voice_tip);
		nav_text = (TextView) findViewById(R.id.nav_text);
		problem_type = (TextView) findViewById(R.id.problem_type);
		problem_type_text = (TextView) findViewById(R.id.problem_type_text);
		voice_desc = (TextView) findViewById(R.id.voice_desc);
		voice_desc_text = (TextView) findViewById(R.id.voice_desc_text);
		input_voice_tip = (TextView) findViewById(R.id.input_voice_tip);
	}

	@Override
	protected void initView() {
		navLeftBtn.setOnClickListener(this);
		inputVoice.setOnTouchListener(this);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.submit:
			if (saveFilePath != null && saveFilePath.exists()) {
				final CustomPopDialog cp = new CustomPopDialog.Builder(ProblemOtherActivity.this).create(9);
				cp.show();
				TextView confirmTv = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm); // 确认退出登录
				TextView dialogCancel = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_cancel); // 取消退出登录
				confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cp.cancel();
						upload_soundFile();
					}
				});
				dialogCancel.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cp.cancel();
					}
				});
			} else {
				ToastTool.showShort(getApplicationContext(), "请先录音再提交问题！谢谢！");
			}
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
		String bikeid = orderPreference.getBike_ID();
		String phone = userPreference.getU_tel();
		String type = getIntent().getStringExtra("type");
		params.put("access_token", userPreference.getAccess_token());
		params.put("bikeid", bikeid);
		params.put("phone", phone);
		params.put("type", type);
		params.put("voiceurl", url);
		params.put("needfrozen", "0");

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "上传成功" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				try {
					if (jsonTool.getStatus().equals("success")) {
						/**
						 * 终止声音，结束声音
						 */
						myRecorder.stop();
						myRecorder.release();
						myRecorder = null;

						String message = jsonObject.getString("message");
						ToastTool.showLong(getApplicationContext(), message);
						AppManager.getInstance().killAllActivityNotCurr();
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "ProblemOtherActivity"); // 这里用来传值
						// 当是其他问题时，跳转回计费页面
						intent.setClass(ProblemOtherActivity.this, AccountAndPersonActivity.class);
						ProblemOtherActivity.this.startActivity(intent);
						ProblemOtherActivity.this.finish();
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
	 * 上传声音文件
	 * 
	 * @Title: upload_soundFile
	 * @Description:
	 * @return: void
	 */
	private void upload_soundFile() {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		try {
			params.put("file", saveFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i(statusCode + "语音上传成功" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();
				String status = jsonTool.getStatus();
				// 如果获取成功时，进行以下设值操作
				if (status.equals("success")) {
					try {
						url = jsonObject.getString("url");
						LogTool.i("语音上传url", "" + url);
						// 一定要做录音操作
						if (saveFilePath != null && saveFilePath.exists()) {
							if (url != null) {
								upload_ques();
							} else {
								ToastTool.showShort(getApplicationContext(), "录音文件上传失败，请重新提交，谢谢！");
							}
						} else {
							ToastTool.showShort(getApplicationContext(), "请先录音再提交问题！谢谢！");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					LogTool.e("失败！");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable throwable) {
				LogTool.e(statusCode + "语音上传失败" + errorResponse);

			}

		};
		AsyncHttpClientTool.post("api/file/uploadvoice", params, responseHandler);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();

		if (v.getId() == R.id.input_voice) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {// 按住说话，保存语音文件
				if (myRecorder == null) {
					setRecorder();
				}
				try {
					voiceTip.setVisibility(View.VISIBLE);
					paths = path + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
							+ count + ".mp3";
					saveFilePath = new File(paths);
					myRecorder.setOutputFile(saveFilePath.getAbsolutePath());
					saveFilePath.createNewFile();
					myRecorder.prepare();
					// 开始录音
					myRecorder.start();
					needRelease = 1;
					LogTool.i("myRecorder", "start" + " " + needRelease);

				} catch (Exception e) {
					ToastTool.showShort(getApplicationContext(), "请长按录音键，放开录音键结束录音！");
					e.printStackTrace();
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {// 放开
				// 执行move滑动后的操作
				voiceTip.setVisibility(View.INVISIBLE);
				if (saveFilePath != null && saveFilePath.exists()) {
					if (needRelease == 1) {
						// myRecorder.stop();
						// myRecorder.release();
						// myRecorder = null;
						needRelease = 0;
						LogTool.i("myRecorder", "stop" + " " + needRelease);
					}
				}
				count++;
			}
		}
		return true;
	}

	/**
	 * 
	 * @Title: setRecorder
	 * @Description: 初始化录音对象
	 * @return: void
	 */
	private void setRecorder() {
		myRecorder = new MediaRecorder();
		// 从麦克风源进行录音
		myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		// 设置输出格式
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		// 设置编码格式
		myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			try {
				path = Environment.getExternalStorageDirectory().getCanonicalPath().toString() + "/ebikeSound";
				File files = new File(path);
				if (!files.exists()) {
					files.mkdir();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non Javadoc)
	 * 
	 * @Title: onResume
	 * 
	 * @Description:
	 * 
	 * @see com.xxn.elephantbike.base.BaseActivity#onResume()
	 */
	// @Override
	// protected void onResume() {
	// super.onResume();
	// // 设置字体
	// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
	// FontManager.changeFonts(root, ProblemOtherActivity.this);
	// }
}
