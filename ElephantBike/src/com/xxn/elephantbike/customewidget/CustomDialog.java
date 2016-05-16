package com.xxn.elephantbike.customewidget;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xxn.elephantbike.R;
import com.xxn.elephantbike.base.AppManager;
import com.xxn.elephantbike.base.BaseApplication;
import com.xxn.elephantbike.ui.AccountAndPersonActivity;
import com.xxn.elephantbike.ui.AccountSubmitActivity;
import com.xxn.elephantbike.ui.LoginOrRegisterActivity;
import com.xxn.elephantbike.utils.AsyncHttpClientTool;
import com.xxn.elephantbike.utils.JsonTool;
import com.xxn.elephantbike.utils.LogTool;
import com.xxn.elephantbike.utils.OrderPreference;
import com.xxn.elephantbike.utils.SharePreference;
import com.xxn.elephantbike.utils.ToastTool;
import com.xxn.elephantbike.utils.UserPreference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialog extends Dialog {

	private static View layout;
	private static Dialog dialog;
	public static UserPreference userPreference = BaseApplication.getInstance().getUserPreference();
	public static SharePreference sharePreference = BaseApplication.getInstance().getSharePreference();
	public static OrderPreference orderPreference = BaseApplication.getInstance().getOrderPreference();

	public CustomDialog(Context context) {
		super(context);
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
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

		public CustomDialog create(int DialogStyle) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			dialog = new CustomDialog(context, R.style.CustomDialog);
			switch (DialogStyle) {
			case 0:
				layout = inflater.inflate(R.layout.dialog_pay_layout, null);
				break;

			case 1:
				layout = inflater.inflate(R.layout.dialog_renew_layout, null);
				break;

			default:
				break;
			}
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// 即使失去对话框焦点也不关闭
			dialog.setCanceledOnTouchOutside(false);

			dialog.setContentView(layout);
			psd_pay_dialog(DialogStyle);
			return (CustomDialog) dialog;
		}

		// 还车结账支付框的特殊化
		public void psd_pay_dialog(final int DialogStyle) {

			final TextView shape_circle_0 = (TextView) layout.findViewById(R.id.shape_circle_0);
			final TextView shape_circle_1 = (TextView) layout.findViewById(R.id.shape_circle_1);
			final TextView shape_circle_2 = (TextView) layout.findViewById(R.id.shape_circle_2);
			final TextView shape_circle_3 = (TextView) layout.findViewById(R.id.shape_circle_3);
			final TextView shape_circle_4 = (TextView) layout.findViewById(R.id.shape_circle_4);
			EditText eText = (EditText) layout.findViewById(R.id.psw_num);
			eText.setFocusableInTouchMode(true);
			eText.requestFocus();
			InputMethodManager inputManager = (InputMethodManager) eText.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInputFromInputMethod(eText.getWindowToken(), 0);

			final View mPayView = (View) layout.findViewById(R.id.pay_status);
			ImageView tView = (ImageView) layout.findViewById(R.id.close_win);
			final EditText pswNum = (EditText) layout.findViewById(R.id.psw_num);
			tView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			eText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					// 该方法中反应出改变输入框之后的所有字符数
					int charNum = s.length();
					shape_circle_0.setVisibility(View.INVISIBLE);
					shape_circle_1.setVisibility(View.INVISIBLE);
					shape_circle_2.setVisibility(View.INVISIBLE);
					shape_circle_3.setVisibility(View.INVISIBLE);
					shape_circle_4.setVisibility(View.INVISIBLE);
					if (charNum >= 1) {
						shape_circle_0.setVisibility(View.VISIBLE);
						shape_circle_0.setText(getStrOfIndex(s.toString(), 1));
					}
					if (charNum >= 2) {
						shape_circle_1.setVisibility(View.VISIBLE);
						shape_circle_1.setText(getStrOfIndex(s.toString(), 2));
					}
					if (charNum >= 3) {
						shape_circle_2.setVisibility(View.VISIBLE);
						shape_circle_2.setText(getStrOfIndex(s.toString(), 3));
					}
					if (charNum >= 4) {
						shape_circle_3.setVisibility(View.VISIBLE);
						shape_circle_3.setText(getStrOfIndex(s.toString(), 4));
					}
					if (charNum >= 5) {
						shape_circle_4.setVisibility(View.VISIBLE);
						shape_circle_4.setText(getStrOfIndex(s.toString(), 5));
						Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.wait_loading);
						LinearInterpolator lin = new LinearInterpolator();
						operatingAnim.setInterpolator(lin);

						mPayView.setVisibility(View.VISIBLE);
						pswNum.setEnabled(false);
						if (DialogStyle == 0) {
							// 匹配还车密码
							checkReturnCode(AccountAndPersonActivity.bikeid + "", s.toString(),
									AccountAndPersonActivity.userPreference.getU_tel());
						} else {
							// 调用恢复密码
							checkRestoreCode(AccountAndPersonActivity.bikeid + "", s.toString(),
									AccountAndPersonActivity.userPreference.getU_tel());
						}
						// 如果该支付密码框为支付输入时
						// if(true){
						// getContext().startActivity(new Intent(getContext(),
						// AccountSubmitActivity.class));
						// dialog.dismiss();
						// }
						// dialog.dismiss();
					}

				}
			});
		}

		/**
		 * 取得一个字符串的第几位
		 * 
		 * @Title: getStrOfIndex 56789
		 * @Description: TODO
		 * @return
		 * @return: String
		 */
		private String getStrOfIndex(String s, int index) {
			String returnStr = "";
			switch (index) {
			case 1:
				returnStr = s.substring(0, 1);
				break;
			case 2:
				returnStr = s.substring(1, 2);
				break;
			case 3:
				returnStr = s.substring(2, 3);
				break;
			case 4:
				returnStr = s.substring(3, 4);
				break;
			case 5:
				returnStr = s.substring(4, 5);
				break;
			default:
				break;
			}
			return returnStr;
		}

		/**
		 * 匹配还车密码
		 * 
		 * @param bikeid
		 * @param pass
		 * @param phone
		 */
		private void checkReturnCode(final String bikeid, String pass, final String phone) {

			RequestParams params = new RequestParams();
			params.put("access_token", userPreference.getAccess_token());
			params.put("bikeid", bikeid);
			params.put("pass", pass);
			params.put("phone", phone);

			TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String response) {
					// TODO Auto-generated method stub
					LogTool.i(statusCode + "===" + response);
					JsonTool jsonTool = new JsonTool(response);
					JSONObject jsonObject = jsonTool.getJsonObject();

					String status = jsonTool.getStatus();
					if (status.equals("success")) {
						// 1、密码匹配成功时关闭对话框
						dialog.dismiss();
						// 2、不再请求获取单车当前的费用和时间
						AccountAndPersonActivity.isContinue = false;
						// 3、密码匹配成功时，看是否车在还车区域中
						// checkBikeLoc(phone, bikeid, getLatLng());
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "AccountAndPersonActivity"); // 这里用来传值
						intent.setClass(getContext(), AccountSubmitActivity.class);
						getContext().startActivity(intent);
						((Activity) getContext()).finish();
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							dialog.dismiss();
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getContext(), LoginOrRegisterActivity.class);
							getContext().startActivity(intent);
							((Activity)getContext()).finish();
						} else {
							LogTool.e("匹配还车密码失败！");
							LogTool.e(message);
							// 此时提示密码错误
							final CustomPopDialog cpd = new CustomPopDialog.Builder(getContext()).create(1);
							cpd.show();
							TextView confirmTv = (TextView) CustomPopDialog.getLayout()
									.findViewById(R.id.dialog_comfirm); // 还车结账密码错误对话框确认
							confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
								@Override
								public void onClick(View v) {
									cpd.cancel();
								}
							});
							dialog.dismiss();
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
					// TODO Auto-generated method stub
					LogTool.e("服务器错误" + errorResponse);
					if (statusCode != 0) {
						JsonTool jsonTool = new JsonTool(errorResponse);
						if (jsonTool.getStatus().equals("fail")) {
							// mPhoneView.setError(jsonTool.getMessage());
							// focusView = mPhoneView;
						}
					}
				}
			};
			AsyncHttpClientTool.post("api/pass/returncode2", params, responseHandler);
		}

		private String getLatLng() {
			String s = "{lng:" + AccountAndPersonActivity.userPreference.getLongitude() + ",lat:"
					+ AccountAndPersonActivity.userPreference.getLatitude() + "}";
			return s;
		}

		/**
		 * 判断单车是否在所划定的区域中
		 * 
		 * @param phone
		 *            手机号
		 * @param bikeid
		 *            单车编号
		 * @param location
		 *            单车位置
		 * @return void 返回类型
		 */
		private void checkBikeLoc(String phone, String bikeid, String location) {
			RequestParams params = new RequestParams();
			params.put("access_token", userPreference.getAccess_token());
			params.put("bikeid", bikeid);
			params.put("phone", phone);
			params.put("location", location);

			TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String response) {
					// TODO Auto-generated method stub
					LogTool.i(statusCode + "===" + response);
					JsonTool jsonTool = new JsonTool(response);
					JSONObject jsonObject = jsonTool.getJsonObject();

					String status = jsonTool.getStatus();
					if (status.equals("success")) {
						dialog.dismiss();
						// 此时做跳转操作，到单车计费页面
						Intent intent = new Intent();
						intent.putExtra("fromActivityTo", "AccountAndPersonActivity"); // 这里用来传值
						intent.setClass(getContext(), AccountSubmitActivity.class);
						getContext().startActivity(intent);
						((Activity) getContext()).finish();
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							dialog.dismiss();
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getContext(), LoginOrRegisterActivity.class);
							getContext().startActivity(intent);
							((Activity)getContext()).finish();
						} else {
							LogTool.e("匹配还车密码失败！");
							LogTool.e(message);
							// 此时提示单车未停放在校园内，还车失败
							final CustomPopDialog cpd = new CustomPopDialog.Builder(getContext()).create(2);
							cpd.show();
							// 设置点击确认关闭提示对话框
							TextView confirmTv = (TextView) CustomPopDialog.getLayout()
									.findViewById(R.id.dialog_comfirm); // 身份认证
							confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
								@Override
								public void onClick(View v) {
									cpd.cancel();
								}
							});
							dialog.dismiss();
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
					// TODO Auto-generated method stub
					LogTool.e("服务器错误" + errorResponse);
					if (statusCode != 0) {
						JsonTool jsonTool = new JsonTool(errorResponse);
						if (jsonTool.getStatus().equals("fail")) {
							LogTool.e("服务器错误");
						}
					}
				}
			};
			AsyncHttpClientTool.post("api/bike/bikelocation", params, responseHandler);
		}

		/**
		 * 调用恢复密码
		 */
		private void checkRestoreCode(String bikeid, String pass, String phone) {

			RequestParams params = new RequestParams();
			params.put("access_token", userPreference.getAccess_token());
			params.put("bikeid", bikeid);
			params.put("pass", pass);
			params.put("phone", phone);

			TextHttpResponseHandler responseHandler = new TextHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String response) {
					// TODO Auto-generated method stub
					LogTool.i(statusCode + "===" + response);
					JsonTool jsonTool = new JsonTool(response);
					JSONObject jsonObject = jsonTool.getJsonObject();

					String status = jsonTool.getStatus();
					if (status.equals("success")) {
						// 1、恢复密码匹配成功时
						// getContext().startActivity(new Intent(getContext(),
						// AccountSubmitActivity.class));
						dialog.dismiss();
						ToastTool.showShort(getContext(), "获取解锁密码成功！");
						// 2、恢复密码匹配成功后获取到的恢复密码然后显示在面板上
					} else {
						String message = jsonTool.getMessage();
						if ("invalid token".equals(message)) {
							dialog.dismiss();
							userPreference.clearAll();
							sharePreference.clearAll();
							orderPreference.clearAll();
							Intent intent = new Intent();
							AppManager.getInstance().killAllActivityExceptThis();
							intent.setClass(getContext(), LoginOrRegisterActivity.class);
							getContext().startActivity(intent);
							((Activity)getContext()).finish();
						} else {
							LogTool.e("匹配还车密码失败！");
							LogTool.e(message);
							dialog.dismiss();
							// 此时提示密码错误
							final CustomPopDialog cpd = new CustomPopDialog.Builder(getContext()).create(1);
							cpd.show();
							TextView confirmTv = (TextView) CustomPopDialog.getLayout()
									.findViewById(R.id.dialog_comfirm); // 身份认证
							confirmTv.setOnClickListener(new android.view.View.OnClickListener() {
								@Override
								public void onClick(View v) {
									cpd.cancel();
								}
							});
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
					// TODO Auto-generated method stub
					LogTool.e("服务器错误" + errorResponse);
					if (statusCode != 0) {
						JsonTool jsonTool = new JsonTool(errorResponse);
						if (jsonTool.getStatus().equals("fail")) {
							// mPhoneView.setError(jsonTool.getMessage());
							// focusView = mPhoneView;
						}
					}
				}
			};
			AsyncHttpClientTool.post("api/pass/restorecode2", params, responseHandler);
		}

	}
}
