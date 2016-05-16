package com.xxn.elephantbike.customewidget;

import com.xxn.elephantbike.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
/**
 * 提示类的对话框（比如完成、结束）
 * @author lzjing
 *
 */
public class CustomTipDialog extends Dialog {

	  private static View layout;
	  static Dialog dialog;

	public CustomTipDialog(Context context) {
		super(context);
	}

	public CustomTipDialog(Context context, int theme) {
		super(context, theme);
	}

	public static View getLayout() {
		return layout;
	}

	public static void setLayout(View layout) {
		CustomTipDialog.layout = layout;
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

		public CustomTipDialog create(int DialogStyle) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			dialog = new CustomTipDialog(context, R.style.CustomTipDialog);
			switch (DialogStyle) {
			case 0:
				// 支付成功
				setLayout(inflater.inflate(R.layout.dialog_pay_success, null));
				break;

			case 1:
				// 还车结账密码错误对话框
				setLayout(inflater.inflate(R.layout.dialog_psw_error, null));
				break;

			default:
				break;
			}
			dialog.addContentView(getLayout(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 即使失去对话框焦点也不关闭
			dialog.setCanceledOnTouchOutside(false);

			dialog.setContentView(getLayout());
			// psd_pay_dialog();
			return (CustomTipDialog) dialog;
		}

	}
}
