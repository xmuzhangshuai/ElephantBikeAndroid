/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xxn.elephantbike.zxingqrcode;

import java.util.Collection;
import java.util.HashSet;

import com.google.zxing.ResultPoint;
import com.xxn.elephantbike.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
			128, 64 };
	private static final long ANIMATION_DELAY = 100L;
//	private static final int OPAQUE = 0xFF;
	private static final int OPAQUE = 0xFF;

	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final int frameColor;
	private final int laserColor;
	private final int resultPointColor;
	private int scannerAlpha;
	private Resources resources;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	/**
	 * 手机的屏幕密度
	 */
	private static float density;
	/**
	 * 四个绿色边角对应的长度
	 */
	private int ScreenRate;
	/**
	 * 四个绿色边角对应的宽度
	 */
	private static final int CORNER_WIDTH = 12;
	/**
	 * 扫描框中的中间线的宽度
	 */
	private int MIDDLE_LINE_WIDTH = 30;
	/**
	 * 扫描框中的中间线的高度
	 */
	private int MIDDLE_LINE_HEIGHT = 30;
	/**
	 * 中间那条线每次刷新移动的距离
	 */
	private static final int SPEEN_DISTANCE = 15;
	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;

	/**
	 * 中间滑动线的最底端位置
	 */
	private int slideBottom;
	/**
	 * 扫描框中的中间线的与扫描框左右的间隙
	 */
	private static final int MIDDLE_LINE_PADDING = 12;
	/**
	 * 字体距离扫描框下面的距离
	 */
	private static final int TEXT_PADDING_TOP = 30;

	boolean isFirst = false;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		density = context.getResources().getDisplayMetrics().density;
		// 将像素转换成dp
		ScreenRate = (int) (30 * density);
		// System.out.println("ScreenRate:"+ScreenRate);
		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint();
		resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		laserColor = resources.getColor(R.color.viewfinder_laser);
		resultPointColor = resources.getColor(R.color.possible_result_points);
		scannerAlpha = 0;
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@Override
	public void onDraw(Canvas canvas) {
		//扫描层
		Rect frame = CameraManager.get().getFramingRect();
//		Bitmap mBitmap = ((BitmapDrawable) getResources().getDrawable(
//				R.drawable.blue_scan_img)).getBitmap();
		Bitmap mBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.scan_barcode)).getBitmap();
		MIDDLE_LINE_WIDTH = mBitmap.getWidth();
		MIDDLE_LINE_HEIGHT = mBitmap.getHeight();
		if (frame == null) {
			return;
		}

		// 初始化中间线滑动的最上边和最下边
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}
		
		// 获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		// 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		// Draw the exterior (i.e. outside the framing rect) darkened
//		paint.setColor(resultBitmap != null ? resultColor : maskColor);
//		canvas.drawRect(0, 0, width, frame.top, paint);
//		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
//		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
//				paint);
//		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
		
		int leftdis = (width - MIDDLE_LINE_WIDTH)/2;
		int bottomdis = (height - MIDDLE_LINE_HEIGHT)/2;
		
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, bottomdis, paint);
		canvas.drawRect(0, bottomdis, leftdis, bottomdis + MIDDLE_LINE_HEIGHT + 1, paint);
		canvas.drawRect(width - leftdis, bottomdis, width, bottomdis + MIDDLE_LINE_HEIGHT + 1,
				paint);
		canvas.drawRect(0, bottomdis + MIDDLE_LINE_HEIGHT, width, height, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			// Draw a two pixel solid black border inside the framing rect
//			paint.setColor(frameColor);
			//将画笔设为透明
			paint.setColor(Color.TRANSPARENT);
			int linewidht = 5;

			/*
			 * canvas.drawRect(15 + frame.left, 15 + frame.top, 15 + (linewidht
			 * + frame.left), 15 + (50 + frame.top), paint); canvas.drawRect(15
			 * + frame.left, 15 + frame.top, 15 + (50 + frame.left), 15 +
			 * (linewidht + frame.top), paint); canvas.drawRect(-15 + ((0 -
			 * linewidht) + frame.right), 15 + frame.top, -15 + (1 +
			 * frame.right), 15 + (50 + frame.top), paint); canvas.drawRect(-15
			 * + (-50 + frame.right), 15 + frame.top, -15 + frame.right, 15 +
			 * (linewidht + frame.top), paint); canvas.drawRect(15 + frame.left,
			 * -15 + (-49 + frame.bottom), 15 + (linewidht + frame.left), -15 +
			 * (1 + frame.bottom), paint); canvas.drawRect(15 + frame.left, -15
			 * + ((0 - linewidht) + frame.bottom), 15 + (50 + frame.left), -15 +
			 * (1 + frame.bottom), paint); canvas.drawRect(-15 + ((0 -
			 * linewidht) + frame.right), -15 + (-49 + frame.bottom), -15 + (1 +
			 * frame.right), -15 + (1 + frame.bottom), paint);
			 * canvas.drawRect(-15 + (-50 + frame.right), -15 + ((0 - linewidht)
			 * + frame.bottom), -15 + frame.right, -15 + (linewidht - (linewidht
			 * - 1) + frame.bottom), paint);
			 */

			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
					+ ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
					+ CORNER_WIDTH, frame.bottom, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.bottom
					- CORNER_WIDTH, frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom
					- ScreenRate, frame.right, frame.bottom, paint);

			// 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
//			paint.setColor(laserColor);
//			slideTop += SPEEN_DISTANCE;
//			if (slideTop + MIDDLE_LINE_HEIGHT >= frame.bottom) {
//				slideTop = frame.top;
//			}

			Paint p = new Paint();
			// LinearGradient lg = new LinearGradient(0, 0, 100, 100, Color.RED,
			// Color.BLUE, Shader.TileMode.MIRROR);
			// LinearGradient lg = new LinearGradient(0, 0, 0, 100, new int[] {
			// Color.rgb(120, 169, 214), Color.rgb(180, 208, 235),
			// Color.rgb(216, 228, 241) }, null, Shader.TileMode.MIRROR);
			// p.setShader(lg);
			
			
//			Rect mSrcRect = new Rect(0, 0, MIDDLE_LINE_WIDTH,
//					MIDDLE_LINE_HEIGHT);
//			Rect mDestRect = new Rect(frame.left, slideTop - MIDDLE_LINE_PADDING, frame.right,
//					slideTop + MIDDLE_LINE_HEIGHT + MIDDLE_LINE_PADDING);
//			Paint mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//			mBitPaint.setFilterBitmap(true);
//			mBitPaint.setDither(true);

//			canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, mBitPaint);
			
			
			// drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop
			// - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING,
			// slideTop + MIDDLE_LINE_WIDTH / 2, p);
			// canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop
			// - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING,
			// slideTop + MIDDLE_LINE_WIDTH / 2, paint);

			// Draw a red "laser scanner" line through the middle to show
			// decoding is active
			paint.setColor(laserColor);
			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
			// int middle = frame.height() / 2 + frame.top;
			// canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
			// middle + 2, paint);

			// int vmiddle = frame.height() / 2 + frame.top;
			// int hmiddle = frame.width() / 2 + frame.left;
			//
			// canvas.drawRect(hmiddle - 20, vmiddle - 1, hmiddle + 20,
			// vmiddle + 2, paint);
			// canvas.drawRect(hmiddle - 1, vmiddle - 20, hmiddle + 2,
			// vmiddle + 20, paint);

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);
		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

}
