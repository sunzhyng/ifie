package net.ifie.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.ifie.app.AppException;
import net.ifie.app.common.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class VideoList extends Entity{

	public final static int CATALOG_ALL = 1;
	
	private int pageSize;
	private int postCount;
	private int lastTime;
	private List<Video> postlist = new ArrayList<Video>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getPostCount() {
		return postCount;
	}
	public int getLastTime(){
		return lastTime;
	}
	public List<Video> getPostlist() {
		return postlist;
	}

	public static VideoList parseJSON(InputStream inputStream) throws IOException, AppException {
		VideoList postlist = new VideoList();
		Video post = null;
        try {
        	String str = StringUtils.read(inputStream);  
        	JSONObject jsonObject = new JSONObject(str);  
            JSONArray array = jsonObject.getJSONArray("content");
            int length = array.length();  
            for(int i=0;i<length;i++){  
            	JSONObject object = array.getJSONObject(i);
            	post = new Video();
            	post.id = object.getString("id");
            	post.setTitle(object.getString("name"));
            	post.setPubDate(object.getString("addtime"));
            	post.setFace(object.getString("thumb_url"));
            	post.setUrl(object.getString("video_url"));
            	postlist.getPostlist().add(post); 
            	post = null; 
            	postlist.lastTime = object.getInt("addtime");
		        postlist.postCount = 0;
		        postlist.pageSize = 20;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	inputStream.close();	
        }      
        return postlist;       
	}

}
