package com.xxn.elephantbike.ui;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.UserPreference;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 
 */
public class NoticeActivity extends BaseActivity implements OnClickListener {
	private ImageView closeBtn;
	View bg;
	private static ImageView activityImg;
	private SharePreference sharePreference;
	private UserPreference userPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice);
		userPreference = BaseApplication.getInstance().getUserPreference();
		sharePreference = BaseApplication.getInstance().getSharePreference();

		findViewById();
		initView();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		closeBtn = (ImageView) findViewById(R.id.close_btn);
		bg = findViewById(R.id.bg);
		activityImg = (ImageView) findViewById(R.id.activity_img);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		closeBtn.setOnClickListener(this);
		bg.getBackground().setAlpha(120);

		// 如果结果中含有链接图片并且成功，则加载并显示
		if ("success".equals(sharePreference.getActivity_status()) && !sharePreference.getImage_url().isEmpty()) {
			imageLoader.displayImage(sharePreference.getImage_url(), activityImg, NoticeActivity.getImageOptions());
			// 同时集成好该图片的链接地址
			activityImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(NoticeActivity.this, WebActivity.class)
							.putExtra("url", sharePreference.getLink_url()) // 将链接地址设置到图片的URL上
							.putExtra("title", getString(R.string.activity_web)));
					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				}
			});
		}
	}

	public static DisplayImageOptions getImageOptions() {
		DisplayImageOptions options = null;
		// 使用DisplayImageOptions.Builder()创建DisplayImageOptions
		options = new DisplayImageOptions.Builder()
//				.showImageOnLoading(R.drawable.idcard_bg)// 设置图片下载期间显示的图片
//				.showImageForEmptyUri(R.drawable.idcard_bg) // 设置图片Uri为空或是错误的时候显示的图片
//				.showImageOnFail(R.drawable.idcard_bg) // 设置图片加载或解码过程中发生错误显示的图片
//				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
//				.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
				// .displayer(new CircleBitmapDisplayer()) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
//		activityImg.setVisibility(View.VISIBLE);
		return options;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.close_btn:
			Intent intent = new Intent();
			intent.putExtra("flag", "finsh");
			setResult(1, intent);
			finish();
			break;

		default:
			break;
		}

	}

}
