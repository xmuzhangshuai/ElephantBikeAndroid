package com.xxn.elephantbike.zxingqrcode;

import java.util.Hashtable;

import com.xxn.elephantbike.R;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.xxn.elephantbike.base.BaseApplication;

/**
 * @author Ryan Tang
 * @author Lee ks 重绘特殊二维码
 */
public final class EncodingHandler {
	private static final int BLACK = 0xff000000;

	// 生成二维码
	public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		return bitmap;
	}

	// 在二维码基础上继续重绘频道二维码
	public static Bitmap createChannelCode(Bitmap qrcodebg, Bitmap icon, String channelName, String channelInfo, String qrcode) {

		if (channelInfo.length() >= 50) {
			channelInfo = channelInfo.substring(0, 50);
			channelInfo += "...";
		}

		int width = qrcodebg.getWidth();
		int height = qrcodebg.getHeight();
		Bitmap src = null;
		try {
			src = createQRCode(qrcode, width - 150);
		} catch (WriterException e) {
			e.printStackTrace();
		}

		// 創建一個新的和SRC長度寬度一樣的位圖
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		// 框架
		canvas.drawBitmap(qrcodebg, 0, 0, null);
		// 頻道icon
		canvas.drawBitmap(icon, (height - width) / 2 - 20, (height - width) / 2 - 20, null);

		// 二維碼
		int w_src = src.getWidth();
		canvas.drawBitmap(src, width / 2 - w_src / 2, height - width + 150, null);

		// 頻道名稱
		TextPaint textPaint = new TextPaint();
		textPaint.setTextSize(BaseApplication.getInstance().getResources().getDimension(R.dimen.share_title_size));

		textPaint.setColor(Color.DKGRAY);
		int w_icon = icon.getWidth();
		int h_icon = icon.getHeight();
		canvas.drawText(channelName, height - width, (height - width) / 2 + (h_icon - 20) / 2, textPaint);
		// 頻道內容
		textPaint.setColor(Color.DKGRAY);
		textPaint.setTextSize(BaseApplication.getInstance().getResources().getDimension(R.dimen.share_content_size));
		// String text =
		// "如何从一个菜鸟蜕变成一个灌篮高手呢?如何从一个菜鸟蜕变成一个灌篮高手呢?如何从一个菜鸟蜕变成一个灌篮高手呢?";
		StaticLayout layout = new StaticLayout(channelInfo, textPaint, width - 120, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
		canvas.translate(80, height - width + 30);
		layout.draw(canvas);

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return bitmap;

	}

	// 在二维码基础上继续重绘频道二维码
	public static Bitmap createChannelCode(Bitmap top, Bitmap icon, Bitmap content, Bitmap blue_content, Bitmap middle, Bitmap bottom, String channelName, String channelInfo,
			String qrcode) {

		Matrix matrix = new Matrix();
		matrix.postScale(1.0f, 10.0f);
		content = Bitmap.createBitmap(content, 0, 0, content.getWidth(), content.getHeight(), matrix, true);

		if (channelInfo.length() < 50) {
			matrix.postScale(1.0f, 1.8f);
		} else if (channelInfo.length() >= 50) {
			matrix.postScale(1.0f, 2.0f);
			channelInfo = channelInfo.substring(0, 50);
			channelInfo += "...";
		}

		blue_content = Bitmap.createBitmap(blue_content, 0, 0, blue_content.getWidth(), blue_content.getHeight(), matrix, true);

		try {
			Bitmap src;
			src = createQRCode(qrcode, 500);

			int _top = top.getHeight();
			int _top_content = top.getHeight() + content.getHeight();
			int _top_content_middle = top.getHeight() + content.getHeight() + middle.getHeight();
			int _top_content_middle_bluecontent = top.getHeight() + content.getHeight() + middle.getHeight() + blue_content.getHeight();

			int width = top.getWidth(), height = _top_content_middle_bluecontent + bottom.getHeight();

			// 創建一個新的和SRC長度寬度一樣的位圖
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);

			// 框架
			canvas.drawBitmap(top, 0, 0, null);
			canvas.drawBitmap(content, 0, _top, null);
			canvas.drawBitmap(middle, 0, _top_content, null);
			canvas.drawBitmap(blue_content, 0, _top_content_middle, null);
			canvas.drawBitmap(bottom, 0, _top_content_middle_bluecontent, null);

			// 頻道icon
			canvas.drawBitmap(icon, 50, 50, null);

			// 二維碼
			int w_src = src.getWidth();
			canvas.drawBitmap(src, width / 2 - w_src / 2, _top_content_middle_bluecontent - 80, null);

			// 頻道名稱
			TextPaint textPaint = new TextPaint();
			textPaint.setTextSize(BaseApplication.getInstance().getResources().getDimension(R.dimen.share_title_size));

			textPaint.setColor(Color.DKGRAY);
			int w_icon = icon.getWidth();
			int h_icon = icon.getHeight();
			canvas.drawText(channelName, 70 + w_icon, 70 + h_icon / 2, textPaint);
			// 頻道內容
			textPaint.setColor(Color.DKGRAY);
			textPaint.setTextSize(BaseApplication.getInstance().getResources().getDimension(R.dimen.share_content_size));
			// String text =
			// "如何从一个菜鸟蜕变成一个灌篮高手呢?如何从一个菜鸟蜕变成一个灌篮高手呢?如何从一个菜鸟蜕变成一个灌篮高手呢?";
			StaticLayout layout = new StaticLayout(channelInfo, textPaint, width - 150, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
			canvas.translate(100, top.getHeight() + content.getHeight() + middle.getHeight() + 20);
			layout.draw(canvas);

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();

			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

}
