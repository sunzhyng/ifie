package net.ifie.app.bean;

import java.io.IOException;
import java.io.InputStream;

import net.ifie.app.AppException;
import net.ifie.app.common.StringUtils;

public class News extends Entity{
	
	private static final long serialVersionUID = 1L;

	public final static int NEWSTYPE_NEWS = 0x00;//0 新闻

	private String title;
	private String url;
	private String body;
	private String pubDate;

	public String getPubDate() {
		return this.pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	public static News parseHTML(InputStream inputStream) throws IOException, AppException {
		News news = null;
        try {        	
        	news = new News();
        	news.setBody(StringUtils.read(inputStream));
        } catch (Exception e) {
			throw AppException.run(e);
        } finally {
        	inputStream.close();	
        }      
        return news;       
	}
	
}
