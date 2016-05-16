package com.xxn.elephantbike.customewidget;

import java.util.List;

import com.xxn.elephantbike.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/** 
 * 类描述 ：自定义
 * 类名： MyMenuDialog.java  
 * Copyright:   Copyright (c)2015    
 * Company:     zhangshuai   
 * @author:     zhangshuai    
 * @version:    1.0    
 * 创建时间:    2015-8-10 下午8:49:34  
*/
public class MyMenuDialog {
	Context context;
	android.app.AlertDialog ad;
	TextView titleView;
	private ListView menuitemListView;
	private List<String> menuitemList;
	private int[] drawables = new int[] {};

	public MyMenuDialog(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		ad = new android.app.AlertDialog.Builder(context).create();
		ad.show();
		// 关键在下面的两行,使用window.setContentView,替换整个对话框窗口的布局
		Window window = ad.getWindow();
		window.setContentView(R.layout.menu_dialog);
		titleView = (TextView) window.findViewById(R.id.title);
		menuitemListView = (ListView) window.findViewById(R.id.dialog_listview);
	}

	public void setListItemClickListener(OnItemClickListener listener) {
		menuitemListView.setOnItemClickListener(listener);
	}

	public void setTitle(String title) {
		titleView.setText(title);
	}

	public void setMenuList(List<String> list) {
		this.menuitemList = list;
	}

	public void setDrawable(int[] drawable) {
		this.drawables = drawable;
	}

	/**
	 * 关闭对话框
	 */
	public void dismiss() {
		ad.dismiss();
	}

	public void show() {
		if (menuitemList != null) {
			menuitemListView.setAdapter(new MenuDialogAdapter());
		}

		ad.setCancelable(true);
		ad.show();
	}

	/**
	 *
	 * 项目名称：quanzi  
	 * 类名称：HomeDialogAdapter  
	 * 类描述：
	 * @author zhangshuai
	 * @date 创建时间：2015-4-27 上午11:52:43 
	 *
	 */
	private class MenuDialogAdapter extends BaseAdapter {
		private class ViewHolder {
			public TextView itemName;
			public TextView righttext;
			public ImageView imageView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return menuitemList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = convertView;
			final ViewHolder holder;

			if (convertView == null) {
				view = LayoutInflater.from(context).inflate(R.layout.menu_dialog_list_item, null);
				holder = new ViewHolder();
				holder.itemName = (TextView) view.findViewById(R.id.item_name);
				holder.righttext = (TextView) view.findViewById(R.id.righttext);
				holder.imageView = (ImageView) view.findViewById(R.id.leftimage);
				view.setTag(holder); // 给View添加一个格外的数据
			} else {
				holder = (ViewHolder) view.getTag(); // 把数据取出来
			}

			holder.itemName.setText(menuitemList.get(position));

			if (drawables.length > 0) {
				holder.imageView.setVisibility(View.VISIBLE);
				holder.imageView.setImageResource(drawables[position]);
			} else {
				holder.imageView.setVisibility(View.GONE);
			}

			return view;
		}
	}
}
