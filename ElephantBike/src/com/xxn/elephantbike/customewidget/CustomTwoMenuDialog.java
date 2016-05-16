package com.xxn.elephantbike.customewidget;

import com.xxn.elephantbike.R;
import com.xxn.elephantbike.ui.BikeLostActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

/**
 * 自定义的弹出式信息对话框，具有两个确认项，比如丢失单车，确认丢失和我再找找这种取消式按钮
 * 
 * @author lzjing
 *
 */
public class CustomTwoMenuDialog extends Dialog {

	private static View layout;
	private static Context c;
	static Dialog dialog;

	public CustomTwoMenuDialog(Context context) {
		super(context);
		this.c = context;
	}

	public CustomTwoMenuDialog(Context context, int theme) {
		super(context, theme);
		this.c = context;
	}

	public static View getLayout() {
		return layout;
	}

	public static void setLayout(View layout) {
		CustomTwoMenuDialog.layout = layout;
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

		public CustomTwoMenuDialog create(int DialogStyle) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			dialog = new CustomTwoMenuDialog(context, R.style.CustomPopDialog);
			switch (DialogStyle) {
			case 0:
				// 单车丢失
				layout = inflater.inflate(R.layout.dialog_bike_lost_confirm, null);
				break;

			case 1:
				// 还车结账密码错误对话框
				layout = inflater.inflate(R.layout.dialog_psw_error, null);
				break;

			default:
				break;
			}
			dialog.addContentView(getLayout(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 即使失去对话框焦点也不关闭
			dialog.setCanceledOnTouchOutside(false);

			dialog.setContentView(getLayout());
			confirmDialog(DialogStyle);
			return (CustomTwoMenuDialog) dialog;
		}

		//确认时关闭或者打开其他窗口页面
		public void confirmDialog(final int DialogStyle) {
			final TextView confirmLost = (TextView) layout.findViewById(R.id.confirm_lost);
			final TextView findAgain = (TextView) layout.findViewById(R.id.find_again);
			
			//确定丢失后跳转到单车寻找界面
			confirmLost.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(c, BikeLostActivity.class);
					c.startActivity(intent);
					dialog.dismiss();
				}
			});
			//再找找后关闭对话框界面不动，不做跳转
			findAgain.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

	}

}
