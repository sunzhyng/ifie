package net.ifie.app.bean;


public class Video extends Entity{

	public final static int CATALOG_ASK = 1;
	public final static int CATALOG_SHARE = 2;
	public final static int CATALOG_OTHER = 3;
	public final static int CATALOG_JOB = 4;
	public final static int CATALOG_SITE = 5;
	
	private String title;
	private String url;
	private String face;
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
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	
}
