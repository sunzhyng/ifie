package net.ifie.app.adapter;

import java.util.List;

import net.ifie.app.bean.Video;
import net.ifie.app.common.BitmapManager;
import net.ifie.app.common.StringUtils;
import net.ifie.app.R;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewVideoAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Video> 					listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源 
	private BitmapManager 				bmpManager;
	static class ListItemView{				//自定义控件集合  
			public ImageView face;
	        public TextView title;  
		    public TextView date;  
	 }  

	public ListViewVideoAdapter(Context context, List<Video> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(), R.drawable.widget_dface_loading));
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
		//自定义视图
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.face = (ImageView)convertView.findViewById(R.id.video_listitem_userface);
			listItemView.title = (TextView)convertView.findViewById(R.id.video_listitem_title);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
		//设置文字和图片
		Video video = listItems.get(position);
		String faceURL = video.getFace();
		
		if(faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)){
			listItemView.face.setImageResource(R.drawable.widget_dface);
		}else{
			bmpManager.loadBitmap(faceURL, listItemView.face);
		}
		listItemView.face.setTag(video);
		
		listItemView.title.setText(video.getTitle());
		listItemView.title.setTag(video);//设置隐藏参数(实体类)
		
		return convertView;
	}
	
}