package com.xxn.elephantbike.customewidget;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.utils.ToastTool;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @Description:自定义对话框
 */
public class CustomProgressDialog extends ProgressDialog {

	private AnimationDrawable mAnimation;
	private Context mContext;
	private ProgressBar mImagePb;
	private String mLoadingTip;
	private TextView mLoadingTv;
	private int count = 0;
	private String oldLoadingTip;
	private int mResid;
	final static int LOGINACTIVITY = 1;
	final static int MAPROUTEACTIVITY = 2;
	final static int SCOREBOARDACTIVITY = 3;
	static int time = 0;
	static boolean flag = true;

	public CustomProgressDialog(Context context, String content, int id) {
		super(context);
		this.mContext = context;
		this.mLoadingTip = content;
		this.mResid = id;
		setCanceledOnTouchOutside(false);
	}

	public CustomProgressDialog(Context context, String content) {
		super(context);
		this.mContext = context;
		this.mLoadingTip = content;
//		setCanceledOnTouchOutside(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		// initData();
		mLoadingTv.setText(mLoadingTip);
	}

	// private void initData() {
	//
	// mImageView.setBackgroundResource(mResid);
	// // 通过ImageView对象拿到背景显示的AnimationDrawable
	// mAnimation = (AnimationDrawable) mImageView.getBackground();
	// // 为了防止在onCreate方法中只显示第一帧的解决方案之一
	// mImageView.post(new Runnable() {
	// @Override
	// public void run() {
	// mAnimation.start();
	//
	// }
	// });
	// mLoadingTv.setText(mLoadingTip);
	//
	// }

	public void setContent(String str) {
		mLoadingTv.setText(str);
	}

	private void initView() {
		setContentView(R.layout.dialog_progress_loading);
		mLoadingTv = (TextView) findViewById(R.id.loadingTv);
		// mImagePb = (ProgressBar) findViewById(R.id.loadingPb);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		// 开始展示的时候去判断网络情况
		networkTimeout(mContext);
		super.show();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		closeFinishNetworkTimeout();
		super.cancel();
	}

	/**
	 * 开始判断网络超时线程
	 */
	public static void networkTimeout(final Context context) {
		flag = true;
		Thread timeouThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (flag) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					time++;
					if (time == 10) {
						time = 0;
						flag = false;
						ToastTool.showLong(context, "网络请求超时，请检查网络");
					}
				}
			}
		});
		timeouThread.start();
	}

	/**
	 * 关闭判断网络超时
	 */
	public static void closeFinishNetworkTimeout() {
		time = 0;
		flag = false;
	}

	/*
	 * @Override public void onWindowFocusChanged(boolean hasFocus) { // TODO
	 * Auto-generated method stub mAnimation.start();
	 * super.onWindowFocusChanged(hasFocus); }
	 */
}
