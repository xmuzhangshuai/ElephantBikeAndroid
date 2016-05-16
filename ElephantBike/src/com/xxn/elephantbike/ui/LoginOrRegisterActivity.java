package com.xxn.elephantbike.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.table.UserTable;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.CommonTools;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginOrRegisterActivity extends BaseActivity {

	private Button startButton;// 注册开始
	private Button checkButton;// 验证按钮
	private EditText phoneText;// 电话号码输入框
	private TextView navText; // 验证手机
	private EditText identifyCode;// 验证码电话号码输入框
	private TextView agreementTextView;// 法律协议条款
	private View navLeftBtn; // 后退按钮
	private TextView codeForgot; // 忘记密码
	private View mProgressView;// 缓冲
	private IntentFilter filter2;
	private BroadcastReceiver smsReceiver;
	private UserPreference userPreference;
	private TimeCount time;
	private String patternCoder = "(?<!\\d)\\d{6}(?!\\d)";
	private View focusView;
	private String phoneTmep;
	private boolean isRun = false;
	private Handler timeComeHandler;//按验证15s秒后显示没收到验证码

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		userPreference = BaseApplication.getInstance().getUserPreference();

		findViewById();
		initView();
		setTypeface();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, LoginOrRegisterActivity.this);

		// 自动提取短信验证码
		filter2 = new IntentFilter();
		filter2.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter2.setPriority(Integer.MAX_VALUE);
		smsReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Object[] objs = (Object[]) intent.getExtras().get("pdus");
				for (Object obj : objs) {
					byte[] pdu = (byte[]) obj;
					SmsMessage sms = SmsMessage.createFromPdu(pdu);
					// 短信的内容
					String message = sms.getMessageBody();
					LogTool.d("验证码", "message     " + message);
					// 短息的手机号。。+86开头？
					String from = sms.getOriginatingAddress();
					LogTool.d("验证码", "from     " + from);
					if (!TextUtils.isEmpty(from)) {
						String code = patternCode(message);
						if (!TextUtils.isEmpty(code)) {
							final String strContent = code;
							new Handler() {
								public void handleMessage(android.os.Message msg) {
									identifyCode.setText(strContent);
								};
							}.sendEmptyMessage(1);
						}
					}
				}
			}
		};
		registerReceiver(smsReceiver, filter2);
	}

	/**
	 * 匹配短信中间的6个数字（验证码等）
	 * 
	 * @param patternContent
	 * @return
	 */
	private String patternCode(String patternContent) {
		if (TextUtils.isEmpty(patternContent)) {
			return null;
		}
		Pattern p = Pattern.compile(patternCoder);
		Matcher matcher = p.matcher(patternContent);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	/**
	 * @Title: setTypeface
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(),
				"MyTypeface.ttc");
		navText.setTypeface(tf);
		checkButton.setTypeface(tf);
		startButton.setTypeface(tf);
		agreementTextView.setTypeface(tf);
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		startButton = (Button) findViewById(R.id.start_button);
		navText = (TextView) findViewById(R.id.nav_text);
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		phoneText = (EditText) findViewById(R.id.phone_num);
		checkButton = (Button) findViewById(R.id.check_button);
		identifyCode = (EditText) findViewById(R.id.identify_code);
		agreementTextView = (TextView) findViewById(R.id.agreement);
		mProgressView = findViewById(R.id.login_status);
		codeForgot = (TextView) findViewById(R.id.code_forgot);
	}

	@Override
	protected void initView() {
		timeComeHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0x001) {
					codeForgot.setVisibility(View.VISIBLE);
				}
			}
		};
		codeForgot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getPhonePsw();
			}
		});

		phoneText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

				if (isRun) {// 这几句要加，不然每输入一个值都会执行两次onTextChanged()，导致堆栈溢出，原因不明
					isRun = false;
					return;
				}
				//
				isRun = true;
				// Log.i("tag", "onTextChanged()");
				// if (isTel) {
				String finalString = s.toString();
				if (finalString.length() == 3 && !finalString.contains(".")) {
					// 做删除操作时不做+" "
					if (!(start == 3 && before == 1 && count == 0)) {
						finalString = finalString + " ";
					} else {
						finalString = finalString.substring(0, finalString.length() - 1);
					}
				}
				if (finalString.length() == 8 && !finalString.contains(".")) {
					// 做删除操作时不做+" "
					if (!(start == 8 && before == 1 && count == 0)) {
						finalString = finalString + " ";
					} else {
						finalString = finalString.substring(0, finalString.length() - 1);
					}
				}
				if (finalString.contains(".") || finalString.contains("-")) {
					finalString = finalString.substring(0, finalString.length() - 1);
				}
				// Log.i("tag", "onTextChanged()");
				phoneText.setText(finalString);
				phoneText.setSelection(finalString.length());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				// 该方法中反应出改变输入框之后的所有字符数
				int charNum = s.toString().length();
				// 手机号码输入正确时
				if (charNum == 13 && !s.toString().contains(".") && !s.toString().contains("-"))
					checkButton.setEnabled(true);
				else
					checkButton.setEnabled(false);
			}

		});
		identifyCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				// 该方法中反应出改变输入框之后的所有字符数
				int charNum = s.length();
				// 手机号码输入正确时
				if (charNum == 6)
					startButton.setEnabled(true);
				else
					startButton.setEnabled(false);
			}
		});

		checkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//首先校验手机号，如果通过执行后面操作
				if(valphone()){
					checkButton.setEnabled(false);
					time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
					time.start();// 开始计时

					// 只要是触发了发送验证码

					phoneTmep = phoneText.getText().toString().replace(" ", "");
					// Drawable drawable =
					// getResources().getDrawable(R.drawable.no_receive);
					// // / 这一步必须要做,否则不会显示.
					// drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					// drawable.getMinimumHeight());
					// identifyCode.setCompoundDrawables(null, null, drawable,
					// null);

					// 开始去请求验证码
					attemptGetCode();
				}
			}
		});
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 点击开始后尝试登录
				attemptLogin();
			}
		});
		navLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// finish();
				// 返回到扫描状态，扫描界面为主界面
				Intent intent = new Intent(LoginOrRegisterActivity.this, CaptureActivity.class);
				startActivity(intent);
				finish();
			}
		});

		agreementTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(LoginOrRegisterActivity.this, WebActivity.class)
				.putExtra("url", "http://www.citi-sense.cn/splmeter_help.html")
				.putExtra("title", getString(R.string.agreementText)));
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
		});

	}

	/**
	 * 得到电话密码
	 * 
	 * @Title: getPhonePsw
	 * @Description: TODO
	 * @return: void
	 */
	private void getPhonePsw() {
		// String phone = userPreference.getU_tel();
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phoneTmep);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				// JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				// 如果获取成功时，进行以下设值操作
				if (status.equals("success")) {
					ToastTool.showShort(getApplicationContext(), "请等待接收来电！");
				} else {
					LogTool.e("获取密码电话失败！");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						LogTool.e("获取密码电话失败！");
					}
				}
			}

		};
		AsyncHttpClientTool.post("api/msg/voicesms", params, responseHandler);
	}

	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			checkButton.setText("重发");
			checkButton.setClickable(true);
			checkButton.setEnabled(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			checkButton.setClickable(false);
			checkButton.setText(millisUntilFinished / 1000 + "秒");
			if(checkButton.getText().toString().equals("45秒")){
				Message msg = new Message();
				msg.what = 0x001;
				timeComeHandler.sendMessage(msg);	
			}
		}
	}

	/**
	 * 尝试获取验证码
	 */
	public void attemptGetCode() {

		// Store values at the time of the login attempt.
		String tel = phoneText.getText().toString().replace(" ", "");

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put(UserTable.U_TEL, tel);

		// 先检查电话号码是否为空
		if (TextUtils.isEmpty(tel)) {
			phoneText.setError(getString(R.string.error_field_required));
		} else if (!CommonTools.isMobileNO(tel)) {
			phoneText.setError(getString(R.string.error_invalid_phone));
		}
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				// JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 成功获取验证码后
					Toast.makeText(LoginOrRegisterActivity.this, "获取验证码成功！",
							Toast.LENGTH_SHORT).show();
					// getUserInfo(jsonObject.getString("user_id"));
					LogTool.i("成功获取验证码后" + message);
				} else {
					LogTool.e(message);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				boolean cancel = false;
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						cancel = true;
					}
				}
			}
		};
		AsyncHttpClientTool.post("api/msg/sms", params, responseHandler);

	}

	/**
	 * 校验手机号
	 * @Title: valohone 
	 * @Description: TODO
	 * @return
	 * @return: Boolean
	 */
	private Boolean valphone() {
		Boolean phoneState = true;
		String phone = phoneText.getText().toString().replace(" ", "");
		// 先检查电话号码是否为空
		if (TextUtils.isEmpty(phone)) {
			phoneText.setError(getString(R.string.error_field_required));
			focusView = phoneText;
			phoneState = false;
		} else if (!CommonTools.isMobileNO(phone)) {
			LogTool.e("***phone:", phone);
			phoneText.setError(getString(R.string.error_invalid_phone));
			focusView = phoneText;
			phoneState = false;
		}
		return phoneState;
	}

	/**
	 * 登录
	 * @Title: attemptLogin
	 * @Description: TODO
	 * @return: void
	 */
	public void attemptLogin() {

		// Reset errors.
		phoneText.setError(null);
		identifyCode.setError(null);

		// Store values at the time of the login attempt.
		String phone = phoneText.getText().toString().replace(" ", "");
		String pswCode = identifyCode.getText().toString();

		boolean cancel = false;

		// 先检查电话号码是否为空
		if (TextUtils.isEmpty(phone)) {
			phoneText.setError(getString(R.string.error_field_required));
			focusView = phoneText;
			cancel = true;
		} else if (!CommonTools.isMobileNO(phone)) {
			LogTool.e("***phone:", phone);
			phoneText.setError(getString(R.string.error_invalid_phone));
			focusView = phoneText;
			cancel = true;
		}

		// 再检查是否输入了验证码
		// Check for a valid pswCode, if the user entered one.
		else if (TextUtils.isEmpty(pswCode)) {
			identifyCode.setError(getString(R.string.error_field_required));
			focusView = identifyCode;
			cancel = true;
		} else if (!CommonTools.isPassValid(pswCode)) {
			identifyCode.setError(getString(R.string.error_pattern_pswCode));
			focusView = identifyCode;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			// login(phone, MD5For32.GetMD5Code(pswCode));
			login(phone, pswCode);

			// 同时存入手机号和密码，用于记住密码登录使用
			userPreference.setU_tel(phone);
			// userPreference.setU_pswCode(MD5For32.GetMD5Code(pswCode));
			userPreference.setU_VERIFY_CODE(pswCode);
		}
	}

	// 登录
	private void login(String tel, final String pswCode) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put(UserTable.U_TEL, tel);
		params.put(UserTable.U_VERIFY_CODE, pswCode);
		// 是否已经登录（0为未登录，1为已经登录过）
		params.put(UserTable.U_IS_LOGIN, 0);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				// showProgress(true);
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(LoginOrRegisterActivity.this,
						true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 等待完成
					LoadingPopTool.popLoadingStyle(
							LoginOrRegisterActivity.this, false);
					// 设置用户登录为真
					userPreference.setUserLogin(true);
					// 保存访问令牌
					jsonTool.saveAccess_token();
					// 登录成功后获取用户信息,并将这些信息保存至本地
					// 将服务器传回来的数据进行处理
					saveUser(jsonObject);
					// getUserInfo(jsonObject.getString("user_id"));
					LogTool.d("登录成功时信息：" + message + "登录是否成功标志："
							+ userPreference.getUserLogin());
					// 登录成功后
					// 此时获取到的userPreference就是最新的了，对用户的支付状态做判断
					if (!(userPreference.getU_IS_FINISH()).isEmpty()
							&& "1".equals(userPreference.getU_IS_FINISH())) {
						// 1、未完成，跳转计费界面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo",
								"LoginOrRegisterActivity"); // 这里用来传值
						intent.setClass(LoginOrRegisterActivity.this,
								AccountAndPersonActivity.class);
						LoginOrRegisterActivity.this.startActivity(intent);
						LoginOrRegisterActivity.this.finish();
					} else if (!(userPreference.getU_IS_PAY()).isEmpty()
							&& "1".equals(userPreference.getU_IS_PAY())) {
						// 2、未支付，跳转至支付界面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo",
								"LoginOrRegisterActivity"); // 这里用来传值
						intent.setClass(LoginOrRegisterActivity.this,
								AccountSubmitActivity.class);
						LoginOrRegisterActivity.this.startActivity(intent);
						LoginOrRegisterActivity.this.finish();
					} else {
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo",
								"LoginOrRegisterActivity"); // 这里用来传值
						intent.setClass(LoginOrRegisterActivity.this,
								CaptureActivity.class);
						LoginOrRegisterActivity.this.startActivity(intent);
						LoginOrRegisterActivity.this.finish();
					}
				} else {
					// showProgress(false);
					// 等待完成
					LoadingPopTool.popLoadingStyle(
							LoginOrRegisterActivity.this, false);
					LogTool.e("登录失败时 " + message);
					// 如果登录失败时
					// pswCode.setError(message);
					// focusView = mPasswordView;
					identifyCode.requestFocus();
					// identifyCode.setError(getString(R.string.error_pattern_pswCode));
					ToastTool.showLong(LoginOrRegisterActivity.this,
							getString(R.string.error_pattern_pswCode));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				boolean cancel = false;
				LogTool.e("服务器错误" + errorResponse);
				if (statusCode != 0) {
					JsonTool jsonTool = new JsonTool(errorResponse);
					if (jsonTool.getStatus().equals("fail")) {
						// mPhoneView.setError(jsonTool.getMessage());
						// focusView = mPhoneView;
						cancel = true;
					}
				}
				if (cancel)
					focusView.requestFocus();
				// showProgress(false);
				// 等待完成
				LoadingPopTool.popLoadingStyle(LoginOrRegisterActivity.this,
						false);
			}
		};
		AsyncHttpClientTool.post("api/user/login", params, responseHandler);
	}
	

	/**
	 * 存储自己的信息
	 * 
	 * @param jsonUserObject
	 */
	private void saveUser(final JSONObject jsonUserObject) {
		// TODO Auto-generated method stub
		try {
			// String userInfo = jsonUserObject.getString("user_info");
			// JSONObject userInfoJsonObject = new JSONObject(userInfo);

			String isfrozen = jsonUserObject.getString(UserTable.U_IS_FROZEN);
			// 用户状态是否冻结
			userPreference.setU_IS_FROZEN(isfrozen);
			String isfinish = jsonUserObject.getString(UserTable.U_IS_FINISH);
			// 订单是否完成
			userPreference.setU_IS_FINISH(isfinish);
			String ispay = jsonUserObject.getString(UserTable.U_IS_PAY);
			// 是否完成支付
			userPreference.setU_IS_PAY(ispay);
			String name = jsonUserObject.getString(UserTable.STU_NAME);
			// 该用户的姓名
			userPreference.setU_NAME(name);
			// String cardNum =
			// jsonUserObject.getString(UserTable.STU_CARD_NUM);
			String college = jsonUserObject.getString(UserTable.STU_COLLEGE);
			// 该用户所在的学校
			userPreference.setU_COLLEGE(college);
			String isVip = jsonUserObject.getString(UserTable.U_IS_VIP);
			// 该用户是否为VIP
			userPreference.setU_is_vip(isVip);
			// String create_time =
			// jsonUserObject.getString(UserTable.U_CREATE_TIME);
			// Date date = DateTimeTools.StringToDate(create_time);
			userPreference.setUserLogin(true);
			// 该用户的卡号
			// userPreference.setU_CARD_NUM(cardNum);
			userPreference.printUserInfo();
			// userPreference.setU_CreatTime(date);
			String ismessage = jsonUserObject.getString(UserTable.U_IS_MESSAGE);
			// 该用户是否有新消息
			userPreference.setU_IS_MESSAGE(ismessage);
		} catch (JSONException e) {
			e.printStackTrace();
			LogTool.e("存储用户全部信息有误");
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (show) {
			// 隐藏软键盘
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(LoginOrRegisterActivity.this
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

}
