package net.ifie.app.adapter;

import java.util.List;

import net.ifie.app.bean.SearchList.Result;
import net.ifie.app.common.StringUtils;
import net.ifie.app.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListViewSearchAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Result> 				listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	static class ListItemView{				//自定义控件集合  
        public TextView title;
	    public TextView date;  
	    public LinearLayout layout;
	}  

	public ListViewSearchAdapter(Context context, List<Result> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
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
	
	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");
		
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.title = (TextView)convertView.findViewById(R.id.search_listitem_title);
			listItemView.date = (TextView)convertView.findViewById(R.id.search_listitem_date);
			listItemView.layout = (LinearLayout)convertView.findViewById(R.id.search_listitem_ll);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
		
		//设置文字和图片
		Result res = listItems.get(position);
		
		listItemView.title.setText(res.getTitle());
		listItemView.title.setTag(res);//设置隐藏参数(实体类)
		if(StringUtils.isEmpty(res.getAuthor())) {
			listItemView.layout.setVisibility(LinearLayout.GONE);
		}else{
			listItemView.layout.setVisibility(LinearLayout.VERTICAL);
			listItemView.date.setText(StringUtils.friendly_time(res.getPubDate()));
		}
		
		return convertView;
	}
}