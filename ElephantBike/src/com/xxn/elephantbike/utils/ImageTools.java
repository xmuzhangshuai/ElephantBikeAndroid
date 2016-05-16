package com.xxn.elephantbike.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;

/**
 * Tools for handler picture
 * 
 * @author Ryan.Tang
 * 
 */
public final class ImageTools {
	public static String getThumbnailImagePath(String imagePath) {
		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
		return path;
	}

	/**
	 * 质量压缩方法
	 * @param image
	 * @return
	 */
	public static Bitmap compressByQueality(Bitmap image, int sizeOfKB) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 60, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > sizeOfKB) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩        
			baos.reset();//重置baos即清空baos
			options -= 15;//每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * 质量压缩方法
	 * @param image
	 * @return
	 */
	public static File compressByQualityForFile(String path, String photoName, Bitmap image, int sizeOfKB) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 60, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > sizeOfKB) { //循环判断如果压缩后图片是否大于sizeOfKB kb,大于继续压缩	
			baos.reset();//重置baos即清空baos
			options -= 15;//每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
		}

		return FileUtil.getFileFromBytes(baos.toByteArray(), path + "/" + photoName);
	}

	/**
	 * 先按比例压缩，在按质量压缩
	 * @param srcPath
	 * @return
	 */
	public static File compressBySizeAndQuality(String path, String photoName, Bitmap bitmap, int sizeOfKB) {
		int degree = ImageTools.readPictureDegree(path + "/" + photoName);
		//把图片旋转为正的方向
		bitmap = ImageTools.rotaingImageView(degree, bitmap);

		return compressByQualityForFile(path, photoName, bitmap, sizeOfKB);//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 先按比例压缩，在按质量压缩
	 * @param srcPath
	 * @return
	 */
	public static File compressBySizeAndQuality(String path, String photoName, String src, int sizeOfKB) {

		int size = (int) FileSizeUtil.getFileOrFilesSize(src, FileSizeUtil.SIZETYPE_KB);
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (size > 3000) {//大于2M
			options.inSampleSize = 3;//图片宽高都为原来的二分之一，即图片为原来的四分之一
		} else if (size > 1500) {
			options.inSampleSize = 2;
		} else {
			options.inSampleSize = 1;
		}

		Bitmap bitmap = BitmapFactory.decodeFile(src, options);
		int degree = ImageTools.readPictureDegree(path + "/" + photoName);
		//把图片旋转为正的方向
		bitmap = ImageTools.rotaingImageView(degree, bitmap);

		return compressByQualityForFile(path, photoName, bitmap, sizeOfKB);//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 先按比例压缩，在按质量压缩
	 * @param srcPath
	 * @return
	 */
	public static Bitmap compressBySizeAndQuality(Bitmap image, int sizeofKB) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressByQueality(bitmap, sizeofKB);//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 图片按比例大小压缩方法（根据路径获取图片并压缩）
	 * @param srcPath
	 * @return
	 */
	public static Bitmap compressBySize(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是1280*720分辨率，所以高和宽我们设置为
		float hh = 1280f;//这里设置高度为1280f
		float ww = 720f;//这里设置宽度为720f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return bitmap;//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 图片按比例大小压缩方法（根据Bitmap图片压缩）：
	 * @param image
	 * @return
	 */
	public static Bitmap compressBySize(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出	
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是1280*720分辨率，所以高和宽我们设置为
		float hh = 1280f;//这里设置高度为1280f
		float ww = 720f;//这里设置宽度为720f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return bitmap;//压缩好比例大小后再进行质量压缩
	}

	/**
	 * 获取bitmap大小
	 * @param bitmap
	 * @return
	 */
	@SuppressLint("NewApi")
	public static long getBitmapsize(Bitmap bitmap) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount() / 1024;
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/*
	 * 旋转图片
	 * 
	 * @param angle
	 * 
	 * @param bitmap
	 * 
	 * @return Bitmap
	 */

	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		//旋转图片 动作   
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片   
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * Transfer drawable to bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * Input stream to bitmap
	 * 
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static Bitmap inputStreamToBitmap(InputStream inputStream) throws Exception {
		return BitmapFactory.decodeStream(inputStream);
	}

	/**
	 * Byte transfer to bitmap
	 * 
	 * @param byteArray
	 * @return
	 */
	public static Bitmap byteToBitmap(byte[] byteArray) {
		if (byteArray.length != 0) {
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		} else {
			return null;
		}
	}

	/**
	 * Byte transfer to drawable
	 * 
	 * @param byteArray
	 * @return
	 */
	public static Drawable byteToDrawable(byte[] byteArray) {
		ByteArrayInputStream ins = null;
		if (byteArray != null) {
			ins = new ByteArrayInputStream(byteArray);
		}
		return Drawable.createFromStream(ins, null);
	}

	/**
	 * Bitmap transfer to bytes
	 * 
	 * @param byteArray
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bm) {
		byte[] bytes = null;
		if (bm != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			bytes = baos.toByteArray();
		}
		return bytes;
	}

	/**
	 * Drawable transfer to bytes
	 * 
	 * @param drawable
	 * @return
	 */
	public static byte[] drawableToBytes(Drawable drawable) {
		BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		Bitmap bitmap = bitmapDrawable.getBitmap();
		byte[] bytes = bitmapToBytes(bitmap);
		;
		return bytes;
	}

	/**
	 * Create reflection images
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w, h / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, h, w, h + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, h, w, bitmapWithReflection.getHeight() + reflectionGap, paint);

		return bitmapWithReflection;
	}

	/**
	 * Get rounded corner images
	 * 
	 * @param bitmap
	 * @param roundPx
	 *            5 10
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * 返回圆形图片
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/**
	 * Resize the bitmap
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	/**
	 * 等比例压缩图片
	 * @param bitmap
	 * @param scale
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	/**
	 * Resize the drawable
	 * @param drawable
	 * @param w
	 * @param h
	 * @return
	 */
	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
		float sx = ((float) w / width);
		float sy = ((float) h / height);
		matrix.postScale(sx, sy);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
		return new BitmapDrawable(newbmp);
	}

	/**
	 * Get images from SD card by path and the name of image
	 * @param photoName
	 * @return
	 */
	public static Bitmap getPhotoFromSDCard(String path, String photoName) {
		Bitmap photoBitmap = BitmapFactory.decodeFile(path + "/" + photoName + ".png");
		if (photoBitmap == null) {
			return null;
		} else {
			return photoBitmap;
		}
	}

	public static Bitmap getPhotoFromSDCard(String path) {
		Bitmap photoBitmap = BitmapFactory.decodeFile(path);
		if (photoBitmap == null) {
			return null;
		} else {
			return photoBitmap;
		}
	}

	/**
	 * Check the SD card 
	 * @return
	 */
	public static boolean checkSDCardAvailable() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * Get image from SD card by path and the name of image
	 * @param fileName
	 * @return
	 */
	public static boolean findPhotoFromSDCard(String path, String photoName) {
		boolean flag = false;

		if (checkSDCardAvailable()) {
			File dir = new File(path);
			if (dir.exists()) {
				File folders = new File(path);
				File photoFile[] = folders.listFiles();
				for (int i = 0; i < photoFile.length; i++) {
					String fileName = photoFile[i].getName().split("\\.")[0];
					if (fileName.equals(photoName)) {
						flag = true;
					}
				}
			} else {
				flag = false;
			}

		} else {
			flag = false;
		}
		return flag;
	}

	/**
	 * Save image to the SD card 
	 * @param photoBitmap
	 * @param photoName
	 * @param path
	 */
	public static File savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName, int quality) {
		if (checkSDCardAvailable()) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File photoFile = new File(path, photoName);
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(photoFile);
				if (photoBitmap != null) {
					if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)) {
						fileOutputStream.flush();
					}
				}
			} catch (FileNotFoundException e) {
				photoFile.delete();
				e.printStackTrace();
			} catch (IOException e) {
				photoFile.delete();
				e.printStackTrace();
			} finally {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return photoFile;
		}
		return null;
	}

	/**
	 * Delete the image from SD card
	 * @param context
	 * @param path
	 * file:///sdcard/temp.jpg
	 */
	public static void deleteAllPhoto(String path) {
		if (checkSDCardAvailable()) {
			File folder = new File(path);
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
	}

	public static void deletePhotoAtPathAndName(String path, String fileName) {
		if (checkSDCardAvailable()) {
			File folder = new File(path);
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().equals(fileName)) {
					files[i].delete();
				}
			}
		}
	}

	public static void deleteImageByPath(String path) {
		File file = new File(path);
		if (file != null) {
			file.delete();
		}
	}

	/**
	 * 读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap ReadBitmapById(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/***
	 * 根据资源文件获取Bitmap
	 * 
	 * @param context
	 * @param drawableId
	 * @return
	 */
	public static Bitmap ReadBitmapById(Context context, int drawableId, int screenWidth, int screenHight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inInputShareable = true;
		options.inPurgeable = true;
		InputStream stream = context.getResources().openRawResource(drawableId);
		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
		return getBitmap(bitmap, screenWidth, screenHight);
	}

	//将Bitmap转换成InputStream 
	public static InputStream bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	/***
	 * 等比例压缩图片
	 * 
	 * @param bitmap
	 * @param screenWidth
	 * @param screenHight
	 * @return
	 */
	public static Bitmap getBitmap(Bitmap bitmap, int screenWidth, int screenHight) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scale = (float) screenWidth / w;
		float scale2 = (float) screenHight / h;

		// scale = scale < scale2 ? scale : scale2;

		// 保证图片不变形.
		matrix.postScale(scale, scale);
		// w,h是原图的属性.
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}

}
