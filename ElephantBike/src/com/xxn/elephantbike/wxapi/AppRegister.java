package com.xxn.elephantbike.wxapi;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxn.elephantbike.config.Constants.WeChatConfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);

		// 将该app注册到微信
		msgApi.registerApp(WeChatConfig.APP_ID);
	}
}
