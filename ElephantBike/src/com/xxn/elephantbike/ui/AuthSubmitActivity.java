package com.xxn.elephantbike.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.config.Constants;
import com.xxn.elephantbike.customewidget.MyMenuDialog;
import com.xxn.elephantbike.table.UserTable;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.FileSizeUtil;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.ImageTools;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LoadingPopTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class AuthSubmitActivity extends BaseActivity implements OnClickListener {

	private View navLeftBtn; // 后退按钮
	private Button cardAuthSubmit; // 提交按钮
	private TextView chooseSchool;// 选择学校输入框
	private EditText inputName;
	private EditText inputStuNumber;
	private TextView submitResultTip; // 提交信息提示
	private UserPreference userPreference;
	private SharePreference sharePreference;
	private Uri lastPhotoUri;
	private int type = 0;
	private ImageView stuCardPhoto;
	private TextView uploadStuCardFront, nav_text;
	Bitmap bitmap;
	Boolean uploadImageFlag = false; // 照片还未上传
	String schoolName, name, stuNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_auth_submit);

		userPreference = BaseApplication.getInstance().getUserPreference();
		sharePreference = BaseApplication.getInstance().getSharePreference();
		findViewById();
		initView();
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, AuthSubmitActivity.this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 将其设为可点击
		chooseSchool.setClickable(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 如果是直接从相册获取
		case 1:
			if (data != null) {
				startPhotoCrop(data.getData());
				Uri uri = data.getData();
				ContentResolver cr = getApplication().getContentResolver();
				try {
					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
					/* 将Bitmap设定到ImageView */
					stuCardPhoto.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					Log.e("Exception", e.getMessage(), e);
				}
			}
			break;
		// 如果是调用相机拍照时
		case 2:
			if (new File(getImagePath()).exists()) {
				startPhotoCrop(Uri.fromFile(new File(getImagePath())));
			}
			break;
		// 取得裁剪后的图片并上传
		case 3:
			uploadImage(lastPhotoUri.getPath());
			break;
		case 4:// 获取学校名字
			schoolName = data.getExtras().getString("schoolName", "0");
			chooseSchool.setText(schoolName);
			break;
		default:
			break;
		}
		// super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void findViewById() {
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		inputName = (EditText) findViewById(R.id.input_name);
		inputStuNumber = (EditText) findViewById(R.id.input_student_number);
		cardAuthSubmit = (Button) findViewById(R.id.card_auth_submit);
		chooseSchool = (TextView) findViewById(R.id.choose_school);
		stuCardPhoto = (ImageView) findViewById(R.id.stu_card_photo);
		uploadStuCardFront = (TextView) findViewById(R.id.upload_stucard_front);
		submitResultTip = (TextView) findViewById(R.id.info_submit_result);
		nav_text = (TextView) findViewById(R.id.nav_text);
	}

	@Override
	protected void initView() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		nav_text.setTypeface(tf);
		uploadStuCardFront.setTypeface(tf);
		navLeftBtn.setOnClickListener(this);
		chooseSchool.setOnClickListener(this);
		// stuCardPhoto.setOnClickListener(this);
		// 如果未审核通过或者未有提交用户审核信息
		if (!"0".equals(userPreference.getU_IS_FROZEN())) {
			// 重新请求用户信息
			getAuthInfo(userPreference.getU_tel(), "");
			// 暴力再取一次（改不完的bug）
			getIdCardUrl(userPreference.getU_tel());
		}
		if (!"2".equals(userPreference.getU_IS_FROZEN())) {
			// 初始化上传照片的文本
			uploadStuCardFront.setText(Html.fromHtml("请拍摄<b><u> 学生卡有信息的一面  </u></b>的照片"));
		}

		// 判断是否重新从网络加载图片到本地，否则直接调用本地显示
		if ("-1".equals(userPreference.getU_IS_FROZEN()) && ("".equals(userPreference.getU_ID_CARD())
				|| "".equals(userPreference.getU_STU_CARD()) || "".equals(userPreference.getU_CARD_NUM()))) {
			// 此时重新请求服务器获得图片信息
			getIdCardUrl(userPreference.getU_tel());
		} else if (!"0".equals(userPreference.getU_IS_FROZEN())) {
			// 显示上传学生证
			imageLoader.displayImage(Constants.AppliactionServerIP + userPreference.getU_STU_CARD(), stuCardPhoto,
					AuthSubmitActivity.getImageOptions());
		}

		stuCardPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 是学生证
				// type = 1;
				showPicturePicker(AuthSubmitActivity.this);
			}
		});

		cardAuthSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 信息是否填写完全
				if (infoIsFinsh()) {
					String phone = userPreference.getU_tel();
					String stucard = userPreference.getU_STU_CARD();
					submitIdentity(phone, stucard);
				}
			}
		});

		// 提交了认证消息，但还未审核完成
		if ("2".equals(userPreference.getU_IS_FROZEN())) {
			// 显示操作,如果更新失败，做提示，并关闭该页面
			if (!userInfoGet()) {
				ToastTool.showLong(AuthSubmitActivity.this, "信息显示出错！");
				this.finish();
			}
			// 对界面进行相应的处理操作
			infoSubmitResult();
			submitResultTip.setText("信息审核中，结果将在2个工作日内通知您");
		} else if ("1".equals(userPreference.getU_IS_FROZEN()) || "-1".equals(userPreference.getU_IS_FROZEN())
				|| "3".equals(userPreference.getU_IS_FROZEN())) {// 审核通过了
			// 显示操作,如果更新失败，做提示，并关闭该页面
			if (!userInfoGet()) {
				ToastTool.showLong(AuthSubmitActivity.this, "信息显示出错！");
				this.finish();
			} else {
				// 信息审核通过并能正常显示
				submitResultTip.setText("已认证：" + chooseSchool.getText());
			}
			// 对界面进行相应的处理操作
			infoSubmitResult();
		}

		// isButGary();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left_btn:
			finish();
			break;
		case R.id.choose_school:
			Intent intent = new Intent(getApplicationContext(), SchoolListActivity.class);
			startActivityForResult(intent, 4);
			chooseSchool.setClickable(false);
			break;
		default:
			break;
		}
	}

	// 提交按钮是否为灰色
	// public void isButGary() {
	// if ("".equals(userPreference.getU_STU_CARD())) {
	// cardAuthSubmit.setEnabled(false);
	// } else {
	// cardAuthSubmit.setEnabled(true);
	// }
	// }

	public static DisplayImageOptions getImageOptions() {
		DisplayImageOptions options = null;
		// 使用DisplayImageOptions.Builder()创建DisplayImageOptions
		options = new DisplayImageOptions.Builder()
				// .showImageOnLoading(R.drawable.idcard_bg)// 设置图片下载期间显示的图片
				// .showImageForEmptyUri(R.drawable.idcard_bg) //
				// 设置图片Uri为空或是错误的时候显示的图片
				// .showImageOnFail(R.drawable.idcard_bg) //
				// 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
				// .displayer(new CircleBitmapDisplayer()) // 设置成圆角图片
				.displayer(new RoundedBitmapDisplayer(15)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
		return options;
	}

	/**
	 * 显示对话框，从拍照和相册选择图片来源
	 * 
	 * @param context
	 * @param isCrop
	 */
	private void showPicturePicker(Context context) {

		final MyMenuDialog myMenuDialog = new MyMenuDialog(AuthSubmitActivity.this);
		myMenuDialog.setTitle("图片来源");
		ArrayList<String> list = new ArrayList<String>();
		list.add("拍照");
		list.add("相册");
		myMenuDialog.setMenuList(list);
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent;
				switch (position) {
				case 0:// 如果是拍照
					myMenuDialog.dismiss();
					intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getNewImagePath())));
					startActivityForResult(intent, 2);
					break;
				case 1:// 从相册选取
					myMenuDialog.dismiss();
					intent = new Intent(Intent.ACTION_PICK, null);
					intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
					startActivityForResult(intent, 1);
					break;

				default:
					break;
				}
			}
		};
		myMenuDialog.setListItemClickListener(listener);
		myMenuDialog.show();
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoCrop(Uri uri) {
		lastPhotoUri = Uri.fromFile(new File(getLastImagePath()));

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 0.55);
		// intent.putExtra("outputX", 800);
		// intent.putExtra("outputY", 800);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 280);
		intent.putExtra("outputY", 150);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoUri);
		intent.putExtra("return-data", false);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		startActivityForResult(intent, 3);
	}

	// 获得新的路径
	private String getNewImagePath() {
		// 删除上一次截图的临时文件
		String path = null;
		SharedPreferences sharedPreferences = getSharedPreferences("temp", Context.MODE_PRIVATE);
		ImageTools.deleteImageByPath(sharedPreferences.getString("tempPath", ""));

		// 保存本次截图临时文件名字
		File file = new File(Environment.getExternalStorageDirectory() + "/idcard");
		if (!file.exists()) {
			file.mkdirs();
		}

		path = file.getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		Editor editor = sharedPreferences.edit();
		editor.putString("tempPath", path);
		editor.commit();
		return path;
	}

	// 获得最终截图路径
	private String getLastImagePath() {
		String path = null;
		SharedPreferences sharedPreferences = getSharedPreferences("temp", Context.MODE_PRIVATE);

		// 保存本次截图临时文件名字
		File file = new File(Environment.getExternalStorageDirectory() + "/idcard");
		if (!file.exists()) {
			file.mkdirs();
		}

		path = file.getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		Editor editor = sharedPreferences.edit();
		editor.putString("tempPath", path);
		editor.commit();
		return path;
	}

	// 获得路径
	private String getImagePath() {
		SharedPreferences sharedPreferences = getSharedPreferences("temp", Context.MODE_PRIVATE);
		return sharedPreferences.getString("tempPath", "");
	}

	/**
	 * 身份认证重新获取
	 * 
	 * @Title: getAuthInfo
	 * @Description: TODO
	 * @param tel
	 * @param pswCode
	 * @return: void
	 */
	private void getAuthInfo(String tel, final String pswCode) {

		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put(UserTable.U_TEL, tel);
		params.put(UserTable.U_VERIFY_CODE, pswCode);
		// 是否已经登录（0为未登录，1为已经登录过）
		params.put(UserTable.U_IS_LOGIN, 1);

		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(AuthSubmitActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				// TODO Auto-generated method stub
				LogTool.i(statusCode + "===" + response);
				JsonTool jsonTool = new JsonTool(response);
				JSONObject jsonObject = jsonTool.getJsonObject();

				String status = jsonTool.getStatus();
				// String message = jsonTool.getMessage();
				if (status.equals("success")) {
					// 等待完成
					LoadingPopTool.popLoadingStyle(AuthSubmitActivity.this, false);
					String isfrozen;
					try {
						isfrozen = jsonObject.getString(UserTable.U_IS_FROZEN);
						// 用户状态是否冻结
						userPreference.setU_IS_FROZEN(isfrozen);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					String message = jsonTool.getMessage();
					if ("invalid token".equals(message)) {
						userPreference.clearAll();
						sharePreference.clearAll();
						orderPreference.clearAll();
						Intent intent = new Intent();
						AppManager.getInstance().killAllActivityExceptThis();
						intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
						getApplicationContext().startActivity(intent);
						((Activity) getApplicationContext()).finish();
					} else {

					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				// TODO Auto-generated method stub
				// 等待完成
				LoadingPopTool.popLoadingStyle(AuthSubmitActivity.this, false);
			}
		};
		AsyncHttpClientTool.post("api/user/login", params, responseHandler);
	}

	/**
	 * 得到该用户的相关信息（大学、学号、姓名）
	 * 
	 * @Title: getIdCardUrl
	 * @Description: TODO
	 * @param phone
	 * @return: void
	 */
	public void getIdCardUrl(final String phone) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("phone", phone);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				// 等待加载ing
				LoadingPopTool.popLoadingStyle(AuthSubmitActivity.this, true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				//// 等待加载完成
				LoadingPopTool.popLoadingStyle(AuthSubmitActivity.this, false);
				LogTool.i("" + statusCode + response);
				JsonTool jsonTool = new JsonTool(response);
				String status = jsonTool.getStatus();
				if (status.equals(JsonTool.STATUS_SUCCESS)) {
					JSONObject jsonObject = jsonTool.getJsonObject();
					if (jsonObject != null) {
						try {
							userPreference.setU_STU_CARD(jsonObject.getString("stucard"));
							userPreference.setU_NAME(jsonObject.getString("name"));
							userPreference.setU_CARD_NUM(jsonObject.getString("stunum"));
							imageLoader.displayImage(Constants.AppliactionServerIP + userPreference.getU_STU_CARD(),
									stuCardPhoto, AuthSubmitActivity.getImageOptions());
							userInfoGet();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				} else {
					String message = jsonTool.getMessage();
					if ("invalid token".equals(message)) {
						userPreference.clearAll();
						sharePreference.clearAll();
						orderPreference.clearAll();
						Intent intent = new Intent();
						AppManager.getInstance().killAllActivityExceptThis();
						intent.setClass(getApplicationContext(), LoginOrRegisterActivity.class);
						getApplicationContext().startActivity(intent);
						((Activity) getApplicationContext()).finish();
					} else if (status.equals(JsonTool.STATUS_FAIL)) {
						LogTool.e("由于卸载，清除数据后重新加载图片失败！");
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				LogTool.e("加载图片失败！" + errorResponse);
			}

		};
		AsyncHttpClientTool.post("api/user/cardurl", params, responseHandler);

	}

	/**
	 * 上传文件
	 * 
	 * @param imageUrl
	 */
	public void uploadImage(final String imageUrl) {
		File dir = new File(imageUrl);
		LogTool.e("图片地址" + imageUrl);
		int fileSize = (int) FileSizeUtil.getFileOrFilesSize(imageUrl, 2);
		LogTool.e("文件大小：" + fileSize + "KB");

		if (dir.exists() && !imageUrl.equals("/") && fileSize < 500 && fileSize > 0) {
			RequestParams params = new RequestParams();
			params.put("access_token", userPreference.getAccess_token());
			try {
				params.put("userfile", dir);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, String response) {
					LogTool.i("" + statusCode + response);
					JsonTool jsonTool = new JsonTool(response);
					String status = jsonTool.getStatus();
					if (status.equals(JsonTool.STATUS_SUCCESS)) {
						ToastTool.showLong(AuthSubmitActivity.this, "上传图片成功！");
						JSONObject jsonObject = jsonTool.getJsonObject();
						if (jsonObject != null) {
							try {
								// if (type == 0) {
								// userPreference.setU_ID_CARD(jsonObject.getString("url"));
								// imageLoader.displayImage(
								// Constants.AppliactionServerIP +
								// userPreference.getU_ID_CARD(),
								// uploadIdcardFront,
								// AuthSubmitActivity.getImageOptions());
								// } else {
								userPreference.setU_STU_CARD(jsonObject.getString("url"));
								imageLoader.displayImage(Constants.AppliactionServerIP + userPreference.getU_STU_CARD(),
										stuCardPhoto, AuthSubmitActivity.getImageOptions());

								// }
								// 每次上传完图片之后都需要判断下是否需要将按钮置灰
								// isButGary();
								// 上传成功后将照片信息上的文字置为不可见
								uploadStuCardFront.setVisibility(View.INVISIBLE);
								// 照片上传工作，做一个标志位
								uploadImageFlag = true;
								modifyUploadImage(jsonObject.getString("url"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					} else if (status.equals(JsonTool.STATUS_FAIL)) {
						LogTool.e("上传图片失败！");
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
					LogTool.e("图片上传失败！" + errorResponse);
				}

				@Override
				public void onFinish() {
					super.onFinish();
					// 删除本地图像
					ImageTools.deleteImageByPath(imageUrl);
					lastPhotoUri = null;
				}
			};
			AsyncHttpClientTool.post("api/file/upload", params, responseHandler);
		} else {
			LogTool.e("本地文件为空");
		}
	}

	/**
	 * 提交身份认证信息
	 * 
	 * @Title: submitIdentity
	 * @Description: TODO
	 * @param phone
	 * @param stucard
	 * @return: void
	 */
	private void submitIdentity(String phone, String stucard) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		name = inputName.getText().toString();
		stuNumber = inputStuNumber.getText().toString();
		params.put("phone", phone);
		params.put("stucard", stucard);
		params.put("college", schoolName);
		params.put("stunum", stuNumber);
		params.put("name", name);
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i("" + statusCode + response);
				JsonTool jsonTool = new JsonTool(response);
				String status = jsonTool.getStatus();
				if (status.equals(JsonTool.STATUS_SUCCESS)) {
					LogTool.i(jsonTool.getMessage());
					// 缓存以及更新用户的信息到本地
					userInfoSave();
					// 提交成功后需要做页面置灰和页面改变操作
					infoSubmitResult();
					submitResultTip.setText("信息审核中，结果将在2个工作日内通知您");
					// 更新用户认证的状态位
					userPreference.setU_IS_FROZEN("2");
					// 结束掉当前的activity
					AuthSubmitActivity.this.finish();
				} else if (status.equals(JsonTool.STATUS_FAIL)) {
					LogTool.e(jsonTool.getMessage());
					ToastTool.showShort(AuthSubmitActivity.this, jsonTool.getMessage());
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				LogTool.e("提交身份认证操作失败！" + errorResponse);
			}
		};
		AsyncHttpClientTool.post(AuthSubmitActivity.this, "api/user/authentication", params, responseHandler);
	}

	/**
	 * 提交成功后需要做页面置灰和页面改变操作
	 * 
	 * @Title: infoSubmitResult
	 * @Description: TODO
	 * @return: void
	 */
	private void infoSubmitResult() {
		cardAuthSubmit.setVisibility(View.INVISIBLE);
		// uploadIdcardFont.setVisibility(View.INVISIBLE);
		uploadStuCardFront.setVisibility(View.INVISIBLE);
		inputStuNumber.setEnabled(false);
		inputName.setText(userPreference.getU_NAME());
		inputName.setEnabled(false);
		// cardAuthSubmit.setEnabled(false);
		chooseSchool.setEnabled(false);
		stuCardPhoto.setEnabled(false);
		// submitResultTip.setVisibility(View.VISIBLE);// 提交信息后显示提示信息在页面的底部
	}

	/**
	 * 缓存用户的信息到本地中
	 * 
	 * @Title: userInfoSave
	 * @Description: TODO
	 * @return: void
	 */
	private void userInfoSave() {
		userPreference.setU_NAME(inputName.getText().toString());
		userPreference.setU_COLLEGE(chooseSchool.getText().toString());
		userPreference.setU_CARD_NUM(inputStuNumber.getText().toString());
	}

	/**
	 * 将缓存本地中的信息取出来
	 * 
	 * @Title: userInfoSave
	 * @Description: TODO
	 * @return: void
	 */
	private Boolean userInfoGet() {
		try {
			inputName.setText(userPreference.getU_NAME());
			chooseSchool.setText(userPreference.getU_COLLEGE());
			inputStuNumber.setText(userPreference.getU_CARD_NUM());
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 信息是否填写完全
	 * 
	 * @Title: infoIsFinsh
	 * @Description: TODO
	 * @return
	 * @return: Boolean
	 */
	private Boolean infoIsFinsh() {
		Boolean flag = true;
		if (!uploadImageFlag) {
			ToastTool.showLong(this, "请上传照片！");
			flag = false;
		} else if ("请选择学校".equals(chooseSchool.getText().toString()) || "".equals(chooseSchool.getText().toString())) {
			ToastTool.showLong(this, "请选择您的学校！");
			flag = false;
		} else if ("".equals(inputName.getText().toString())) {
			ToastTool.showLong(this, "请输入您的姓名！");
			flag = false;
		} else if ("".equals(inputStuNumber.getText().toString())) {
			ToastTool.showLong(this, "请填写您的学号！");
			flag = false;
		}
		return flag;
	}

	/**
	 * 修改上传图片
	 * 
	 * @param url
	 */

	private void modifyUploadImage(String url) {
		RequestParams params = new RequestParams();
		params.put("access_token", userPreference.getAccess_token());
		params.put("avatar", url);
		params.put("access_token", userPreference.getAccess_token());
		TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, String response) {
				LogTool.i("" + statusCode + response);
				JsonTool jsonTool = new JsonTool(response);
				String status = jsonTool.getStatus();
				if (status.equals(JsonTool.STATUS_SUCCESS)) {
					LogTool.i(jsonTool.getMessage());
				} else if (status.equals(JsonTool.STATUS_FAIL)) {
					LogTool.e(jsonTool.getMessage());
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
				LogTool.e("修改失败！" + errorResponse);
			}
		};

		// AsyncHttpClientTool.post(AuthSubmitActivity.this,
		// "api/user/modifyAvatar", params, responseHandler);
		AsyncHttpClientTool.post(AuthSubmitActivity.this, "api/file/upload", params, responseHandler);
	}

}