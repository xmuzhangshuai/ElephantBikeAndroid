package com.xxn.elephantbike.ui;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.customewidget.CustomPopDialog;
import com.xxn.elephantbike.customewidget.SlidingMenu;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.FontManager;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;
import com.xxn.elephantbike.zxingqrcode.CameraManager;
import com.xxn.elephantbike.zxingqrcode.CaptureActivityHandler;
import com.xxn.elephantbike.zxingqrcode.InactivityTimer;
import com.xxn.elephantbike.zxingqrcode.ViewfinderView;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 二维码扫描页
 * 
 * @ClassName: CaptureActivity
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年3月20日 下午2:11:45
 */
public class CaptureActivity extends BaseActivity implements Callback {

	// UI references.

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;

	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Button cancelScanButton;
	private Button openLightButton;
	private View idcardAuth;
	private TextView confirmView;
	private TextView frezView;
	private TextView idcardAuthText;
	private TextView statusTip;
	private TextView captureText;
	private View personInfoBtn;
	private UserPreference userPreference;
	private SharePreference sharePreference;
	private int sign = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_capture);
		CameraManager.init(getApplication());
		userPreference = BaseApplication.getInstance().getUserPreference();
		sharePreference = BaseApplication.getInstance().getSharePreference();

		findViewById();
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		initView();
		setTypeface();
		LogTool.i("***************启动一次***************");
		// 设置字体
		// ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		// FontManager.changeFonts(root, CaptureActivity.this);
	}

	/**
	 * @Title: setTypeface
	 * @Description: TODO
	 * @return: void
	 */
	private void setTypeface() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		captureText.setTypeface(tf);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 将其设为可点击的状态
		personInfoBtn.setClickable(true);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	protected void findViewById() {
		Typeface tf = Typeface.createFromAsset(this.getAssets(), "MyTypeface.ttc");
		// person_info = (ImageView) findViewById(R.id.person_info_btn);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		openLightButton = (Button) this.findViewById(R.id.open_light_button);
		cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
		idcardAuth = (View) this.findViewById(R.id.idcard_auth);
		statusTip = (TextView) this.findViewById(R.id.status_tip);
		personInfoBtn = (View) this.findViewById(R.id.person_info_btn);
		idcardAuthText = (TextView) this.findViewById(R.id.idcard_auth_text);
		captureText = (TextView) this.findViewById(R.id.capture_tip_text);

		idcardAuthText.setTypeface(tf);
		statusTip.setTypeface(tf);
		// mMenu = (SlidingMenu) findViewById(R.id.id_menu);
		// moneyRemainMuch = (TextView) findViewById(R.id.money_remain_much);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		personInfoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 将其设为不可点击的状态
				personInfoBtn.setClickable(false);
				if (userPreference.getUserLogin()) {
					Intent intent = new Intent();
					intent.putExtra("fromActivityTo", "CaptureActivity"); // 这里用来传值
					intent.setClass(CaptureActivity.this, PersonInfoActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent();
					intent.putExtra("fromActivityTo", "CaptureActivity"); // 这里用来传值
					intent.setClass(CaptureActivity.this, LoginOrRegisterActivity.class);
					startActivity(intent);
				}
			}
		});

		openLightButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (sign % 2 == 0) {
					// 打开闪关灯
					CameraManager.get().openF();
					openLightButton.setBackgroundResource(R.drawable.open_light_button);
				} else {
					CameraManager.get().stopF();
					openLightButton.setBackgroundResource(R.drawable.unopen_light_button);
				}
				sign++;
			}
		});
		// quit the scan view
		cancelScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("fromActivityTo", "CaptureActivity"); // 这里用来传值
				intent.setClass(CaptureActivity.this, AccountAndPersonActivity.class);
				startActivity(intent);
				// CaptureActivity.this.finish();
			}
		});

		idcardAuth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("fromActivityTo", "CaptureActivity"); // 这里用来传值
				intent.setClass(CaptureActivity.this, AuthSubmitActivity.class);
				startActivity(intent);
				// CaptureActivity.this.finish();
			}
		});

		// userPreference.printUserInfo();

		// 获取账户是否冻结的信息，进行显示,-1时表示冻结状态
		if ("-1".equals(userPreference.getU_IS_FROZEN()) || "3".equals(userPreference.getU_IS_FROZEN())) {
			statusTip.setVisibility(View.VISIBLE);
			// isScan = false;
		} else if ("0".equals(userPreference.getU_IS_FROZEN())) {// 只有注册时（状态位为0）时是可见的
			idcardAuth.setVisibility(View.VISIBLE);
		}

		// 如果含有活动页面则启动活动页面 &&
		// ("1".equals(sharePreference.getACTIVITY_isdisplay()))
		if ("success".equals(sharePreference.getActivity_status())) {
			// isScan = false;
			Intent intent = new Intent();
			intent.setClass(CaptureActivity.this, NoticeActivity.class);
			// startActivityForResult(intent, -1);
			startActivity(intent);
			// 有展示过该活动页，后面不再展示
			sharePreference.setACTIVITY_isdisplay("0");
		}

	}

	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { // if (resultCode == 1) { // Bundle bundle =
	 * data.getExtras(); // String str = bundle.getString("back"); // isScan =
	 * true; // } }
	 */

	/**
	 * Handler scan result
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		// if (isScan) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		// FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		} else {
			// System.out.println("Result:"+resultString);
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtra("fromActivityTo", "CaptureActivity"); // 这里用来传值
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
			System.out.println(resultIntent.getExtras().getBundle("result"));
			// 扫描成功时所需要做的操作,-1:冻结 0注册 1认证
			String state = userPreference.getU_IS_FROZEN();
			if (state.equals("0")) {
				// 此时需要去认证
				final CustomPopDialog cpd = new CustomPopDialog.Builder(CaptureActivity.this).create(0);
				cpd.show();

				idcardAuth = (TextView) CustomPopDialog.getLayout().findViewById(R.id.idcard_auth); // 身份认证
				idcardAuth.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(CaptureActivity.this, AuthSubmitActivity.class);
						startActivity(intent);
						cpd.dismiss();
					}
				});

			} else if ("1".equals(state)) {
				// 正常状态，进入个人中心
				resultIntent.setClass(CaptureActivity.this, AccountAndPersonActivity.class);
				startActivity(resultIntent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				CaptureActivity.this.finish();
			} else if ("2".equals(state)) {
				// 需要等待审核
				final CustomPopDialog cpd = new CustomPopDialog.Builder(CaptureActivity.this).create(3);
				cpd.show();

				confirmView = (TextView) CustomPopDialog.getLayout().findViewById(R.id.dialog_comfirm); // 确认
				confirmView.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cpd.dismiss();
					}
				});
			} else if ("-1".equals(state) || "3".equals(state)) {
				// 该账户已经冻结
				final CustomPopDialog cpd = new CustomPopDialog.Builder(CaptureActivity.this).create(7);
				cpd.show();

				frezView = (TextView) CustomPopDialog.getLayout().findViewById(R.id.frez_comfirm); // 冻结
				frezView.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						cpd.dismiss();
					}
				});
			} else {
				resultIntent.setClass(CaptureActivity.this, LoginOrRegisterActivity.class);
				startActivity(resultIntent);
				finish();
			}
		}
		// }
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}