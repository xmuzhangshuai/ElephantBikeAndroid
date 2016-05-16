package com.xxn.elephantbike.customewidget;

import com.xxn.elephantbike.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
/**
 * 自定义的弹出式等待、加载等旋转进度条
 * @author lzjing
 *
 */
public class CustomLoadingDialog extends Dialog {

	  private static View layout;
	  static Dialog dialog;

	public CustomLoadingDialog(Context context) {
		super(context);
	}

	public CustomLoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	public static View getLayout() {
		return layout;
	}

	public static void setLayout(View layout) {
		CustomLoadingDialog.layout = layout;
	}
	
	public static class Builder {
		private Context context;
		private View contentView;

		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * @return the context
		 */
		public Context getContext() {
			return context;
		}

		/**
		 * @param context
		 *            the context to set
		 */
		public void setContext(Context context) {
			this.context = context;
		}

		/**
		 * @return the contentView
		 */
		public View getContentView() {
			return contentView;
		}

		/**
		 * @param contentView
		 *            the contentView to set
		 */
		public void setContentView(View contentView) {
			this.contentView = contentView;
		}

		public CustomLoadingDialog create(int DialogStyle) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			dialog = new CustomLoadingDialog(context, R.style.CustompopLoadingDialog);
			switch (DialogStyle) {
			case 0:
				// 等待进度条样式一
				setLayout(inflater.inflate(R.layout.dialog_loading, null));
				break;
			default:
				break;
			}
			dialog.addContentView(getLayout(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 即使失去对话框焦点也不关闭
			dialog.setCanceledOnTouchOutside(false);

			dialog.setContentView(getLayout());
			// psd_pay_dialog();
			return (CustomLoadingDialog) dialog;
		}

	}
}
