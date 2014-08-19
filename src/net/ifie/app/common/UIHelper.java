package net.ifie.app.common;

import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickAction;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import net.ifie.app.AppContext;
import net.ifie.app.AppException;
import net.ifie.app.AppManager;
import net.ifie.app.api.ApiClient;
import net.ifie.app.bean.News;
import net.ifie.app.bean.Paper;
import net.ifie.app.bean.Video;
import net.ifie.app.bean.Report;
import net.ifie.app.bean.URLs;
import net.ifie.app.ui.About;
import net.ifie.app.ui.ImageDialog;
import net.ifie.app.ui.ImageZoomDialog;
import net.ifie.app.ui.Main;
import net.ifie.app.ui.NewsDetail;
import net.ifie.app.ui.PaperDetail;
import net.ifie.app.ui.VideoDetail;
import net.ifie.app.ui.Search;
import net.ifie.app.ui.Setting;
import net.ifie.app.ui.ReportDetail;
import net.ifie.app.widget.LinkView;
import net.ifie.app.widget.LinkView.MyURLSpan;
import net.ifie.app.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UIHelper {
	private final static String TAG = "UIHelper";
	
	public final static int LISTVIEW_ACTION_INIT = 0x01;
	public final static int LISTVIEW_ACTION_REFRESH = 0x02;
	public final static int LISTVIEW_ACTION_SCROLL = 0x03;
	public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

	public final static int LISTVIEW_DATA_MORE = 0x01;
	public final static int LISTVIEW_DATA_LOADING = 0x02;
	public final static int LISTVIEW_DATA_FULL = 0x03;
	public final static int LISTVIEW_DATA_EMPTY = 0x04;

	public final static int LISTVIEW_DATATYPE_NEWS = 0x01;
	public final static int LISTVIEW_DATATYPE_VIDEO = 0x03;
	public final static int LISTVIEW_DATATYPE_REPORT = 0x04;
	public final static int LISTVIEW_DATATYPE_PAPER = 0x05;

	public final static int REQUEST_CODE_FOR_RESULT = 0x01;
	public final static int REQUEST_CODE_FOR_REPLY = 0x02;

	/** 全局web样式 */
	// 链接样式文件，代码块高亮的处理
	public final static String linkCss = "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script>"
			+ "<script type=\"text/javascript\" src=\"file:///android_asset/brush.js\"></script>"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\">"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\">"
			+ "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>";
	public final static String WEB_STYLE = linkCss + "<style>* {font-size:14px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;overflow: auto;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";
	/**
	 * 显示首页
	 * 
	 * @param activity
	 */
	public static void showHome(Activity activity) {
		Intent intent = new Intent(activity, Main.class);
		activity.startActivity(intent);
		activity.finish();
	}

	public static void showNewsDetail(Context context, String id, String title, String url) {
		Intent intent = new Intent(context, NewsDetail.class);
		intent.putExtra("news_id", id);
		intent.putExtra("title", title);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}

	public static void showVideoDetail(Context context, String id, String title, String url) {
		Intent intent = new Intent(context, VideoDetail.class);
		intent.putExtra("video_id", id);
		intent.putExtra("title", title);
		intent.putExtra("url", url);
		context.startActivity(intent);
	}
	
	public static void showPaperDetail(Context context, String id, String title, String url) {
		PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    "cn.wps.moffice_eng", 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo ==null){
        	Intent intent = new Intent(context, PaperDetail.class);
    		intent.putExtra("paper_id", id);
    		intent.putExtra("title", title);
    		intent.putExtra("url", url);
    		context.startActivity(intent);
        }else{
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");
			url = Uri.encode(url);
	    	url = url.replaceAll("%3A%2F%2F", "://");
	    	url = url.replaceAll("%2F", "/");
			Uri uri = Uri.parse(url);
			intent.setData(uri);
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
        }
	}
	
	public static void showReportDetail(Context context, String id, String title, String url) {
		PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    "cn.wps.moffice_eng", 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo ==null){
        	Intent intent = new Intent(context, ReportDetail.class);
    		intent.putExtra("report_id", id);
    		intent.putExtra("title", title);
    		intent.putExtra("url", url);
    		context.startActivity(intent);
        }else{
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");
			url = Uri.encode(url);
	    	url = url.replaceAll("%3A%2F%2F", "://");
	    	url = url.replaceAll("%2F", "/");
			Uri uri = Uri.parse(url);
			intent.setData(uri);
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
        }
	}

	public static void showImageDialog(Context context, String imgUrl) {
		Intent intent = new Intent(context, ImageDialog.class);
		intent.putExtra("img_url", imgUrl);
		context.startActivity(intent);
	}

	public static void showImageZoomDialog(Context context, String imgUrl) {
		Intent intent = new Intent(context, ImageZoomDialog.class);
		intent.putExtra("img_url", imgUrl);
		context.startActivity(intent);
	}

	public static void showSetting(Context context) {
		Intent intent = new Intent(context, Setting.class);
		context.startActivity(intent);
	}

	public static void showSearch(Context context, int mc) {
		Intent intent = new Intent(context, Search.class);
		intent.putExtra("cur", mc);
		context.startActivity(intent);
	}
	
	/**
	 * 加载显示图片
	 * 
	 * @param imgFace
	 * @param faceURL
	 * @param errMsg
	 */
	public static void showLoadImage(final ImageView imgView,
			final String imgURL, final String errMsg) {
		// 读取本地图片
		if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
			Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
					R.drawable.widget_dface);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 是否有缓存图片
		final String filename = FileUtils.getFileName(imgURL);
		// Environment.getExternalStorageDirectory();返回/sdcard
		String filepath = imgView.getContext().getFilesDir() + File.separator
				+ filename;
		File file = new File(filepath);
		if (file.exists()) {
			Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 从网络获取&写入图片缓存
		String _errMsg = imgView.getContext().getString(
				R.string.msg_load_image_fail);
		if (!StringUtils.isEmpty(errMsg))
			_errMsg = errMsg;
		final String ErrMsg = _errMsg;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					imgView.setImageBitmap((Bitmap) msg.obj);
					try {
						// 写图片缓存
						ImageUtils.saveImage(imgView.getContext(), filename,
								(Bitmap) msg.obj);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ToastMessage(imgView.getContext(), ErrMsg);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Bitmap bmp = ApiClient.getNetBitmap(imgURL);
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * url跳转
	 * 
	 * @param context
	 * @param url
	 */
	public static void showUrlRedirect(Context context, String url) {
		URLs urls = URLs.parseURL(url);
		if (urls != null) {
			showLinkRedirect(context, urls.getObjType(), urls.getObjId(),
					urls.getObjKey());
		} else {
			openBrowser(context, url);
		}
	}

	public static void showLinkRedirect(Context context, int objType,
			String objId, String objKey) {
			openBrowser(context, objKey);
	}

	/**
	 * 打开浏览器
	 * 
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url) {
		try {
			Uri uri = Uri.parse(url);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
			ToastMessage(context, "无法浏览此网页", 500);
		}
	}

	/**
	 * 获取webviewClient对象
	 * 
	 * @return
	 */
	public static WebViewClient getWebViewClient() {
		return new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				showUrlRedirect(view.getContext(), url);
				return true;
			}
		};
	}

	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}

	/**
	 * 显示关于我们
	 * 
	 * @param context
	 */
	public static void showAbout(Context context) {
		Intent intent = new Intent(context, About.class);
		context.startActivity(intent);
	}

	/**
	 * 快捷栏是否显示文章图片
	 * 
	 * @param activity
	 * @param qa
	 */
	public static void showSettingIsLoadImage(Activity activity, QuickAction qa) {
		if (((AppContext) activity.getApplication()).isLoadImage()) {
			qa.setIcon(MyQuickAction.buildDrawable(activity,
					R.drawable.ic_menu_picnoshow));
			qa.setTitle(activity.getString(R.string.main_menu_picnoshow));
		} else {
			qa.setIcon(MyQuickAction.buildDrawable(activity,
					R.drawable.ic_menu_picshow));
			qa.setTitle(activity.getString(R.string.main_menu_picshow));
		}
	}

	/**
	 * 文章是否加载图片显示
	 * 
	 * @param activity
	 */
	public static void changeSettingIsLoadImage(Activity activity) {
		AppContext ac = (AppContext) activity.getApplication();
		if (ac.isLoadImage()) {
			ac.setConfigLoadimage(false);
			ToastMessage(activity, "已设置文章不加载图片");
		} else {
			ac.setConfigLoadimage(true);
			ToastMessage(activity, "已设置文章加载图片");
		}
	}

	public static void changeSettingIsLoadImage(Activity activity, boolean b) {
		AppContext ac = (AppContext) activity.getApplication();
		ac.setConfigLoadimage(b);
	}

	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void clearAppCache(Activity activity) {
		final AppContext ac = (AppContext) activity.getApplication();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ToastMessage(ac, "缓存清除成功");
				} else {
					ToastMessage(ac, "缓存清除失败");
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context cont,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 发送异常报告
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟器
						i.setType("message/rfc822"); // 真机
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "zhangdeyi@oschina.net" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"开源中国Android客户端 - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						cont.startActivity(Intent.createChooser(i, "发送错误报告"));
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.show();
	}

	public static void Exit(final Context cont) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_menu_surelogout);
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@SuppressLint("JavascriptInterface")
	public static void addWebImageShow(final Context cxt, WebView wv) {
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new OnWebViewImageListener() {

			@Override
			public void onImageClick(String bigImageUrl) {
				if (bigImageUrl != null)
					UIHelper.showImageZoomDialog(cxt, bigImageUrl);
			}
		}, "mWebViewImageListener");
	}
	
}
