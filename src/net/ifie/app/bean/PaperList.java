package net.ifie.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.ifie.app.AppException;
import net.ifie.app.common.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;


public class PaperList extends Entity{

	public final static int CATALOG_ALL = 1;
	
	private int catalog;
	private int pageSize;
	private int newsCount;
	private int lastTime;
	private List<Paper> replist = new ArrayList<Paper>();
	
	public int getCatalog() {
		return catalog;
	}
	public int getPageSize() {
		return pageSize;
	}
	public int getNewsCount() {
		return newsCount;
	}
	public int getLastTime(){
		return lastTime;
	}
	public List<Paper> getInformlist() {
		return replist;
	}
	
	public static PaperList parseJSON(InputStream inputStream) throws IOException, AppException {
		PaperList rlist = new PaperList();
		Paper paper = null;
        try {
        	String str = StringUtils.read(inputStream);  
        	JSONObject jsonObject = new JSONObject(str);  
            JSONArray array = jsonObject.getJSONArray("content");
            int length = array.length();  
            for(int i=0;i<length;i++){  
            	JSONObject object = array.getJSONObject(i);
            	paper = new Paper();
            	paper.id = object.getString("id");
            	paper.setTitle(object.getString("name"));
            	paper.setUrl(object.getString("preview_url"));
            	paper.setFileUrl(object.getString("file_url"));
            	paper.setPubDate(object.getString("addtime"));
            	rlist.getInformlist().add(paper); 
		        paper = null; 
		        rlist.lastTime = object.getInt("addtime");
		        rlist.catalog = 1;
		        rlist.newsCount = 0;
		        rlist.pageSize = 20;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	inputStream.close();	
        }      
        return rlist;       
	}
	
}
