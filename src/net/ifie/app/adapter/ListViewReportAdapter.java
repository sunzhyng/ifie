package net.ifie.app.adapter;

import java.util.List;

import net.ifie.app.bean.Report;
import net.ifie.app.common.StringUtils;
import net.ifie.app.common.UIHelper;
import net.ifie.app.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewReportAdapter extends BaseAdapter {
	private Context context;// 运行上下文
	private List<Report> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	static class ListItemView { // 自定义控件集合
		public TextView date;
		public TextView title;
	}

	public ListViewReportAdapter(Context context, List<Report> data, int resource) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
	}

	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// 自定义视图
		ListItemView listItemView = null;

		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();
			// 获取控件对象
			listItemView.title = (TextView) convertView.findViewById(R.id.report_listitem_title);
			listItemView.date = (TextView) convertView.findViewById(R.id.report_listitem_date);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字和图片
		Report report = listItems.get(position);
		listItemView.title.setText(report.getTitle());
		listItemView.title.setTag(report);// 设置隐藏参数(实体类)
		listItemView.date.setText(StringUtils.getDateStr(report.getPubDate()));

		return convertView;
	}


}