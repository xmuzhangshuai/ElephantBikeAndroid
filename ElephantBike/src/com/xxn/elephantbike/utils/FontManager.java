/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: FontManager.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.utils 
 * @Description: TODO
 * @author: Administrator   
 * @date: 2016年4月12日 上午10:53:01 
 * @version: V1.0   
 */
package com.xxn.elephantbike.utils;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** 
 * @ClassName: FontManager 
 * @Description: 字体替换
 * @author: 张勋
 * @date: 2016年4月12日 上午10:53:01  
 */
public class FontManager {
	public static void changeFonts(ViewGroup root, Activity act) {   
		Typeface tf = null;
		if(tf == null){
			tf = Typeface.createFromAsset(act.getAssets(), "MyTypeface.ttc");
		}
		for (int i = 0; i < root.getChildCount(); i++) {   
			View v = root.getChildAt(i);   
			if (v instanceof TextView) {   
				((TextView) v).setTypeface(tf);   
			} else if (v instanceof Button) {   
				((Button) v).setTypeface(tf);   
			} else if (v instanceof EditText) {   
				((EditText) v).setTypeface(tf);   
			} else if (v instanceof ViewGroup) {   
				changeFonts((ViewGroup) v, act);   
			}   
		}   

	}   
}
