package net.ifie.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.ifie.app.AppException;
import net.ifie.app.common.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReportList extends Entity{
	
	public final static int CATALOG_ALL = 1;

	private int pageSize;
	private int tweetCount;
	private int lastTime;
	private List<Report> reportlist = new ArrayList<Report>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getTweetCount() {
		return tweetCount;
	}
	public int getLastTime(){
		return lastTime;
	}
	public List<Report> getReportlist() {
		return reportlist;
	}
	public static ReportList parseJSON(InputStream inputStream) throws IOException, AppException {
		ReportList tweetlist = new ReportList();
		Report report = null;
        try {
        	String str = StringUtils.read(inputStream);  
        	JSONObject jsonObject = new JSONObject(str);  
            JSONArray array = jsonObject.getJSONArray("content");
            int length = array.length();  
            for(int i=0;i<length;i++){  
            	JSONObject object = array.getJSONObject(i);
            	report = new Report();
            	report.id = object.getString("id");
            	report.setTitle(object.getString("name"));
            	report.setPubDate(object.getString("addtime"));
            	report.setUrl(object.getString("preview_url"));
            	report.setFileUrl(object.getString("file_url"));
            	tweetlist.getReportlist().add(report); 
            	report = null; 
            	tweetlist.lastTime = object.getInt("addtime");
            	tweetlist.tweetCount = 0;
            	tweetlist.pageSize = 20;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	inputStream.close();	
        }      
        return tweetlist;       
	}
	
}
