package net.ifie.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.ifie.app.AppException;
import net.ifie.app.common.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 搜索列表实体类
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class SearchList extends Entity{

	private static final long serialVersionUID = 1L;
	public final static String CATALOG_ALL = "all";
	public final static String CATALOG_NEWS = "news";
	public final static String CATALOG_POST = "post";
	public final static String CATALOG_SOFTWARE = "software";
	public final static String CATALOG_BLOG = "blog";
	public final static String CATALOG_CODE = "code";
	
	private int pageSize;
	private int lastTime;
	private List<Result> resultlist = new ArrayList<Result>();
	
	/**
	 * 搜索结果实体类
	 */
	public static class Result implements Serializable {

		private static final long serialVersionUID = 1L;
		private String objid;
		private int type;
		private String title;
		private String url;
		private String pubDate;
		private String author;
		public String getObjid() {return objid;}
		public void setObjid(String objid) {this.objid = objid;}
		public int getType() {return type;}
		public void setType(int type) {this.type = type;}
		public String getTitle() {return title;}
		public void setTitle(String title) {this.title = title;}
		public String getUrl() {return url;}
		public void setUrl(String url) {this.url = url;}
		public String getPubDate() {return pubDate;}
		public void setPubDate(String pubDate) {this.pubDate = pubDate;}
		public String getAuthor() {return author;}
		public void setAuthor(String author) {this.author = author;}
	}

	public int getPageSize() {
		return pageSize;
	}
	public int getLastTime(){
		return lastTime;
	}
	public List<Result> getResultlist() {
		return resultlist;
	}
	public void setResultlist(List<Result> resultlist) {
		this.resultlist = resultlist;
	}
	
	public static SearchList parseJSON(InputStream inputStream, int mc) throws IOException, AppException {
		SearchList searchList = new SearchList();
		Result res = null;
        try {
        	String str = StringUtils.read(inputStream);  
        	JSONObject jsonObject = new JSONObject(str);  
            JSONArray array = jsonObject.getJSONArray("content");
            if(array == null)
            	return searchList;
            int length = array.length();  
            for(int i=0;i<length;i++){  
            	JSONObject object = array.getJSONObject(i);
            	res = new Result();
            	res.setObjid(object.getString("id"));
            	res.setType(mc);
            	switch(mc) {
            		case 0:
            			res.setTitle(object.getString("title"));
                    	res.setUrl(object.getString("news_url"));
            			break;
            		case 1:
            			res.setTitle(object.getString("name"));
                    	res.setUrl(object.getString("video_url"));
            			break;
            		case 2:
            			res.setTitle(object.getString("name"));
                    	res.setUrl(object.getString("file_url"));
            			break;
            		case 3:
            			res.setTitle(object.getString("name"));
                    	res.setUrl(object.getString("file_url"));
            			break;
            	}
            	res.setPubDate(object.getString("addtime"));
            	searchList.getResultlist().add(res); 
            	searchList.lastTime = object.getInt("addtime");
		        res = null; 
		        searchList.pageSize = 20;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	inputStream.close();	
        }      
        return searchList;       
	}
	
}
