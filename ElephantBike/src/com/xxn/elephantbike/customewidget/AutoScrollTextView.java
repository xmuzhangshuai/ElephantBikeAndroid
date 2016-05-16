package com.xxn.elephantbike.customewidget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
/**
 * 
 */
public class AutoScrollTextView  extends TextView {
	
	private int duration = 1500;
	private int number;

	public float getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
		setText(String.valueOf(number));
		
	}

	public AutoScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 显示
	 * @param number
	 */
	public void runWithAnimation(int preNumber,int number){
		ObjectAnimator objectAnimator = ObjectAnimator.ofInt(
				this, "number", preNumber, number);
		objectAnimator.setDuration(duration);
		objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		objectAnimator.start();
		
	}

}
