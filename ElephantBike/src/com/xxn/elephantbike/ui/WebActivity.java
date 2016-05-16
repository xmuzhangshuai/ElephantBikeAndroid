package com.xxn.elephantbike.ui;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.BaseActivity;
import com.xxn.elephantbike.utils.FontManager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * 各种html页面
 * 
 * @ClassName: WebActivity
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年4月5日 上午1:39:05
 */
public class WebActivity extends BaseActivity {

	private View navLeftBtn; // 后退按钮
	private WebView webView;
	private TextView navText;
	private String url;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_web);

		url = getIntent().getStringExtra("url");
		title = getIntent().getStringExtra("title");
		if (TextUtils.isEmpty(url)) {
			finish();
			return;
		}

		findViewById();
		initView();
		// 设置字体
		ViewGroup root = (ViewGroup)this.getWindow().getDecorView();
		FontManager.changeFonts(root, WebActivity.this);
	}

	@Override
	protected void findViewById() {
		// TODO Auto-generated method stub
		navLeftBtn = (View) findViewById(R.id.nav_left_btn);
		webView = (WebView) findViewById(R.id.webView);
		navText = (TextView) findViewById(R.id.nav_text);
	}

	@Override
	protected void initView() {
		// TODO Auto-generated method stub
		navText.setText(title);

		// 设置javasctipt可用
		// webView.getSettings().setJavaScriptEnabled(true);
		// 加载url，但是不会显示
		webView.loadUrl(url);
		// 指定显示控件(class)
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}
		});

		navLeftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

}
