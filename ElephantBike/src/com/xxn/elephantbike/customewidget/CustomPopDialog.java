package com.xxn.elephantbike.customewidget;

import com.xxn.elephantbike.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
/**
 * 自定义的弹出式信息对话框，比如输入密码错误，确认，帮助信息（弹出计费规则）
 * @author lzjing
 *
 */
public class CustomPopDialog extends Dialog {

	  private static View layout;
	  static Dialog dialog;

	public CustomPopDialog(Context context) {
		super(context);
	}

	public CustomPopDialog(Context context, int theme) {
		super(context, theme);
	}

	public static View getLayout() {
		return layout;
	}

	public static void setLayout(View layout) {
		CustomPopDialog.layout = layout;
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

		public CustomPopDialog create(int DialogStyle) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			dialog = new CustomPopDialog(context, R.style.CustomPopDialog);
			switch (DialogStyle) {
			case 0:
				// 身份认证提醒
				setLayout(inflater.inflate(R.layout.dialog_auth_identity, null));
				break;

			case 1:
				// 还车结账密码错误对话框
				setLayout(inflater.inflate(R.layout.dialog_psw_error, null));
				break;
				
			case 2:
				// 此时提示单车未停放在校园内，还车失败
				setLayout(inflater.inflate(R.layout.dialog_loc_error, null));
				break;
				
			case 3:
				// 需要等待审核
				setLayout(inflater.inflate(R.layout.dialog_check_not, null));
				break;
				
			case 4:
				// 帮助信息弹框
				setLayout(inflater.inflate(R.layout.dialog_help_account, null));
				break;
			case 5:
				// 学校列表
				setLayout(inflater.inflate(R.layout.activity_auth_submit, null));
				break;
				
			case 6:
				// 确认退出弹框
				setLayout(inflater.inflate(R.layout.dialog_confirm_exit, null));
				break;
				
			case 7:
				// 账户被冻结
				setLayout(inflater.inflate(R.layout.dialog_frez_comfirm, null));
				break;
				
			case 8:
				// 余额不足，需要充值
				setLayout(inflater.inflate(R.layout.dialog_recharge_need, null));
				break;
				
			case 9:
				// 确定提交问题
				setLayout(inflater.inflate(R.layout.dialog_submit_confirm, null));
				break;

			default:
				break;
			}
			dialog.addContentView(getLayout(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 即使失去对话框焦点也不关闭
			dialog.setCanceledOnTouchOutside(false);

			dialog.setContentView(getLayout());
			// psd_pay_dialog();
			return (CustomPopDialog) dialog;
		}

	}
}
