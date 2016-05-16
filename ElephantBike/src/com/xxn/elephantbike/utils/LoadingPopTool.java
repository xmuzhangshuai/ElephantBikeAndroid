package com.xxn.elephantbike.utils;

import com.xxn.elephantbike.customewidget.CustomLoadingDialog;

import android.content.Context;

/**
 * 加载进度条的显示
 * 
 * @ClassName: LoadingPopTool
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年4月6日 下午1:10:34
 */
public class LoadingPopTool {

	public static CustomLoadingDialog cp = null;

	public static void popLoadingStyle(Context c, Boolean isShow) {

		if (isShow) {
			if (cp == null) {
				cp = new CustomLoadingDialog.Builder(c).create(0);
			}
			if (cp != null && !(cp.isShowing())){
				cp.show();
			}
		} else {
			if (cp == null) {
//				ToastTool.showShort(c, "已经加载结束！");
				LogTool.i("已经加载结束！");
			} else if(cp.isShowing()){
				cp.cancel();
				cp = null;
			}
		}
	}
}