/**   
 * Copyright © 2016 Lzjing. All rights reserved.
 * 
 * @Title: TelEdittext.java 
 * @Prject: ElephantBike
 * @Package: com.xxn.elephantbike.customewidget 
 * @Description: TODO
 * @author: lzjing   
 * @date: 2016年4月14日 下午12:26:07 
 * @version: V1.0   
 */
package com.xxn.elephantbike.customewidget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/** 
 * @ClassName: TelEdittext 
 * @Description: TODO
 * @author: lzjing
 * @date: 2016年4月14日 下午12:26:07  
 */
public class TelEdittext extends EditText{
    public boolean isTel;
    private String addString=" ";
    private boolean isRun=false;
     
    public TelEdittext(Context context) {
        this(context,null);
    }
     
    public TelEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
     
 
    private void init() {
        addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i("tag", "onTextChanged()之前");
                if(isRun){//这几句要加，不然每输入一个值都会执行两次onTextChanged()，导致堆栈溢出，原因不明
                    isRun = false;
                    return;
                }
//                
                isRun = true;
//                Log.i("tag", "onTextChanged()");
//                if (isTel) {
            	 String finalString= s.toString();
            	if(s.toString().length() == 3){
            		//做删除操作时
            		if(!(start == 3 && before == 1 && count ==0)){
            		finalString = s + " ";
            		}
            	}
            	if(s.toString().length() == 8){
            		//做删除操作时
            		if(!(start == 8 && before == 1 && count ==0)){
            		finalString = s + " ";
            		}
            	}
            	 Log.i("tag", "onTextChanged()");
            	TelEdittext.this.setText(finalString);
            	TelEdittext.this.setSelection(finalString.length());
            	
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
