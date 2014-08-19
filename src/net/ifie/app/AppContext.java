package net.ifie.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import net.ifie.app.api.ApiClient;
import net.ifie.app.bean.News;
import net.ifie.app.bean.NewsList;
import net.ifie.app.bean.Notice;
import net.ifie.app.bean.VideoList;
import net.ifie.app.bean.PaperList;
import net.ifie.app.bean.SearchList;
import net.ifie.app.bean.ReportList;
import net.ifie.app.common.MethodsCompat;
import net.ifie.app.common.StringUtils;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.CacheManager;

@SuppressWarnings("deprecation")
public class AppContext extends Application {
	
	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	public static final int PAGE_SIZE = 20;//默认分页大小
	private static final int CACHE_TIME = 60*60000;//缓存失效时间
	
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
	
	@Override
	public void onCreate() {
		super.onCreate();
        //注册App异常崩溃处理器
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
        
        init();
	}

	/**
	 * 初始化
	 */
	private void init(){

	}
	
	/**
	 * 检测当前系统声音是否为正常模式
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE); 
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}
	
	/**
	 * 应用程序是否发出提示音
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}
	
	/**
	 * 检测网络是否可用
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
	 */
	@SuppressLint("DefaultLocale")
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
	
	/**
	 * 判断当前版本是否兼容目标版本的方法
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}
	
	/**
	 * 获取App安装包信息
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try { 
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
		if(info == null) info = new PackageInfo();
		return info;
	}
	
	/**
	 * 获取App唯一标识
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if(StringUtils.isEmpty(uniqueID)){
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}
	
	public NewsList getNewsList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		NewsList list = null;
		String key = "newslist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getNewsList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (NewsList)readObject(key);
				if(list == null)
					throw e;
			}		
		} else {
			list = (NewsList)readObject(key);
			if(list == null)
				list = new NewsList();
		}
		return list;
	}
	
	public PaperList getPaperList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		PaperList list = null;
		String key = "paperlist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getPaperList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (PaperList)readObject(key);
				if(list == null)
					throw e;
			}		
			
		} else {
			list = (PaperList)readObject(key);
			if(list == null)
				list = new PaperList();
		}
		return list;
	}
	
	public News getNews(String news_id, String url, boolean isRefresh) throws AppException {		
		News news = null;
		String key = "news_"+news_id;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				news = ApiClient.getNewsDetail(this, news_id, url);
				if(news != null){
					Notice notice = news.getNotice();
					news.setNotice(null);
					news.setCacheKey(key);
					saveObject(news, key);
					news.setNotice(notice);
				}
			}catch(AppException e){
				news = (News)readObject(key);
				if(news == null)
					throw e;
			}
		} else {
			news = (News)readObject(key);
			if(news == null)
				news = new News();
		}
		return news;		
	}
	
	public VideoList getVideoList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		VideoList list = null;
		String key = "videolist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {		
			try{
				list = ApiClient.getVideoList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (VideoList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (VideoList)readObject(key);
			if(list == null)
				list = new VideoList();
		}
		return list;
	}

	public ReportList getReportList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		ReportList list = null;
		String key = "reportlist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;		
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getReportList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}
			catch(AppException e){
				list = (ReportList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (ReportList)readObject(key);
			if(list == null)
				list = new ReportList();
		}
		return list;
	}
	
	public SearchList getSearchList(int catalog, String content, int pageIndex, int pageSize) throws AppException {
		return ApiClient.getSearchList(this, catalog, content, pageIndex, pageSize);
	}
	
	public boolean isLoadImage()
	{
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		//默认是加载的
		if(StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}
	
	public void setConfigLoadimage(boolean b)
	{
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}
	
	public boolean isVoice()
	{
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		//默认是开启提示声音
		if(StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}
	
	public void setConfigVoice(boolean b)
	{
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}
	
	public boolean isScroll()
	{
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		//默认是关闭左右滑动
		if(StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}
	
	public void setConfigScroll(boolean b)
	{
		setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
	}
	
	public void cleanCookie()
	{
		removeProperty(AppConfig.CONF_COOKIE);
	}
	
	private boolean isReadDataCache(String cachefile)
	{
		return readObject(cachefile) != null;
	}
	
	private boolean isExistDataCache(String cachefile)
	{
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists())
			exist = true;
		return exist;
	}
	
	public boolean isCacheDataFailure(String cachefile)
	{
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists() && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if(!data.exists())
			failure = true;
		return failure;
	}
	
	public void clearAppCache()
	{
		//清除webview缓存
		File file = CacheManager.getCacheFileBaseDir();  
		if (file != null && file.exists() && file.isDirectory()) {  
		    for (File item : file.listFiles()) {  
		    	item.delete();  
		    }  
		    file.delete();  
		}  		  
		deleteDatabase("webview.db");  
		deleteDatabase("webview.db-shm");  
		deleteDatabase("webview.db-wal");  
		deleteDatabase("webviewCache.db");  
		deleteDatabase("webviewCache.db-shm");  
		deleteDatabase("webviewCache.db-wal");  
		//清除数据缓存
		clearCacheFolder(getFilesDir(),System.currentTimeMillis());
		clearCacheFolder(getCacheDir(),System.currentTimeMillis());
		//2.2版本才有将应用缓存转移到sd卡的功能
		if(isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)){
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),System.currentTimeMillis());
		}
		//清除编辑器保存的临时内容
		Properties props = getProperties();
		for(Object key : props.keySet()) {
			String _key = key.toString();
			if(_key.startsWith("temp"))
				removeProperty(_key);
		}
	}	
	
	private int clearCacheFolder(File dir, long curTime) {          
	    int deletedFiles = 0;         
	    if (dir!= null && dir.isDirectory()) {             
	        try {                
	            for (File child:dir.listFiles()) {    
	                if (child.isDirectory()) {              
	                    deletedFiles += clearCacheFolder(child, curTime);          
	                }  
	                if (child.lastModified() < curTime) {     
	                    if (child.delete()) {                   
	                        deletedFiles++;           
	                    }    
	                }    
	            }             
	        } catch(Exception e) {       
	            e.printStackTrace();    
	        }     
	    }       
	    return deletedFiles;     
	}
	
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}
	
	public Object getMemCache(String key){
		return memCacheRegion.get(key);
	}
	
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try{
			fos = openFileOutput("cache_"+key+".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try{
			fis = openFileInput("cache_"+key+".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
			} catch (Exception e) {}
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	public Serializable readObject(String file){
		if(!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
			//反序列化失败 - 删除缓存文件
			if(e instanceof InvalidClassException){
				File data = getFileStreamPath(file);
				data.delete();
			}
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}

	public boolean containsProperty(String key){
		Properties props = getProperties();
		 return props.containsKey(key);
	}
	
	public void setProperties(Properties ps){
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties(){
		return AppConfig.getAppConfig(this).get();
	}
	
	public void setProperty(String key,String value){
		AppConfig.getAppConfig(this).set(key, value);
	}
	
	public String getProperty(String key){
		return AppConfig.getAppConfig(this).get(key);
	}
	public void removeProperty(String...key){
		AppConfig.getAppConfig(this).remove(key);
	}

}
