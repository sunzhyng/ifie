package net.ifie.app.ui;

import net.ifie.app.AppContext;
import net.ifie.app.AppException;
import net.ifie.app.bean.News;
import net.ifie.app.common.UIHelper;
import net.ifie.app.R;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 新闻详情
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class NewsDetail extends BaseActivity {

	private LinearLayout mHeader;
	private ImageView mHome;
	private ImageView mRefresh;
	private ProgressBar mProgressbar;
	private ScrollView mScrollView;
	private TextView mTitle;

	private WebView mWebView;
	private Handler mHandler;
	private News newsDetail;
	private String newsId;
	private String title;
	private String url;


	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;


	private GestureDetector gd;
	private boolean isFullScreen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_detail);

		this.initView();
		this.initData();

		// 注册双击全屏事件
		this.regOnDoubleEvent();
	}
	
	// 初始化视图控件
	@SuppressLint("SetJavaScriptEnabled")
	private void initView() {
		newsId = getIntent().getStringExtra("news_id");
		title = getIntent().getStringExtra("title");
		url = getIntent().getStringExtra("url");

		mHeader = (LinearLayout) findViewById(R.id.news_detail_header);
		mHome = (ImageView) findViewById(R.id.news_detail_home);
		mRefresh = (ImageView) findViewById(R.id.news_detail_refresh);
		mProgressbar = (ProgressBar) findViewById(R.id.news_detail_head_progress);
		mScrollView = (ScrollView) findViewById(R.id.news_detail_scrollview);
		
		mTitle = (TextView) findViewById(R.id.news_detail_head_title);
        mTitle.setText(title);
		
		mWebView = (WebView) findViewById(R.id.news_detail_webview);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDefaultFontSize(15);
        UIHelper.addWebImageShow(this, mWebView);
        
		mHome.setOnClickListener(homeClickListener);
		mRefresh.setOnClickListener(refreshClickListener);
		mRefresh.setVisibility(View.GONE);
	}

	// 初始化控件数据
	@SuppressLint("HandlerLeak")
	private void initData() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					headButtonSwitch(DATA_LOAD_COMPLETE);
					
					String body = UIHelper.WEB_STYLE + newsDetail.getBody();
					
					// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
					boolean isLoadImage;
					AppContext ac = (AppContext) getApplication();
					if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
						isLoadImage = true;
					} else {
						isLoadImage = ac.isLoadImage();
					}
					if (isLoadImage) {
						// 过滤掉 img标签的width,height属性
						body = body.replaceAll(
								"(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
						body = body.replaceAll(
								"(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

						// 添加点击图片放大支持
						body = body.replaceAll("(<img[^>]+src=\")(\\S+)\"",
								"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");

					} else {
						// 过滤掉 img标签
						body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
					}

					body += "<div style='margin-bottom: 80px'/>";

					mWebView.loadDataWithBaseURL(null, body, "text/html",
							"utf-8", null);
					mWebView.setWebViewClient(UIHelper.getWebViewClient());

				} else if (msg.what == 0) {
					headButtonSwitch(DATA_LOAD_FAIL);

					UIHelper.ToastMessage(NewsDetail.this,
							R.string.msg_load_is_null);
				} else if (msg.what == -1 && msg.obj != null) {
					headButtonSwitch(DATA_LOAD_FAIL);

					((AppException) msg.obj).makeToast(NewsDetail.this);
				}
			}
		};

		initData(newsId, url, false);
	}

	private void initData(final String news_id, final String url, final boolean isRefresh) {
		headButtonSwitch(DATA_LOAD_ING);
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					newsDetail = ((AppContext) getApplication()).getNews(
							news_id, url, isRefresh);
					msg.what = (newsDetail != null && newsDetail.getId() != "") ? 1
							: 0;
					msg.obj = (newsDetail != null) ? newsDetail.getNotice()
							: null;// 通知信息
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 头部按钮展示
	 * 
	 * @param type
	 */
	private void headButtonSwitch(int type) {
		switch (type) {
		case DATA_LOAD_ING:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.VISIBLE);
			//mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
			mScrollView.setVisibility(View.VISIBLE);
			mProgressbar.setVisibility(View.GONE);
			//mRefresh.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_FAIL:
			mScrollView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.GONE);
			//mRefresh.setVisibility(View.VISIBLE);
			break;
		}
	}

	private View.OnClickListener homeClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			UIHelper.showHome(NewsDetail.this);
		}
	};

	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			initData(newsId, url, true);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		if (data == null)
			return;
	}

	/**
	 * 注册双击全屏事件
	 */
	private void regOnDoubleEvent() {
		gd = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						isFullScreen = !isFullScreen;
						if (!isFullScreen) {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
							getWindow().setAttributes(params);
							getWindow()
									.clearFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							mHeader.setVisibility(View.VISIBLE);
						} else {
							WindowManager.LayoutParams params = getWindow()
									.getAttributes();
							params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
							getWindow().setAttributes(params);
							getWindow()
									.addFlags(
											WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							mHeader.setVisibility(View.GONE);
						}
						return true;
					}
				});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (isAllowFullScreen()) {
			gd.onTouchEvent(event);
		}
		return super.dispatchTouchEvent(event);
	}
}
