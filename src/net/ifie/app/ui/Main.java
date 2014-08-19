package net.ifie.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.ifie.app.AppContext;
import net.ifie.app.AppException;
import net.ifie.app.adapter.ListViewNewsAdapter;
import net.ifie.app.adapter.ListViewVideoAdapter;
import net.ifie.app.adapter.ListViewPaperAdapter;
import net.ifie.app.adapter.ListViewReportAdapter;
import net.ifie.app.bean.Paper;
import net.ifie.app.bean.News;
import net.ifie.app.bean.NewsList;
import net.ifie.app.bean.Notice;
import net.ifie.app.bean.Video;
import net.ifie.app.bean.VideoList;
import net.ifie.app.bean.PaperList;
import net.ifie.app.bean.Report;
import net.ifie.app.bean.ReportList;
import net.ifie.app.common.StringUtils;
import net.ifie.app.common.UIHelper;
import net.ifie.app.widget.BadgeView;
import net.ifie.app.widget.NewDataToast;
import net.ifie.app.widget.PullToRefreshListView;
import net.ifie.app.widget.ScrollLayout;
import net.ifie.app.R;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Main extends BaseActivity {
	private ScrollLayout mScrollLayout;
	private RadioButton[] mButtons;
	private String[] mHeadTitles;
	private int mViewCount;
	private int mCurSel;

	private TextView mHeadTitle;
	private ProgressBar mHeadProgress;
	private ImageButton mHead_search;

	private int curNewsCatalog = NewsList.CATALOG_ALL;
	private int curVideoCatalog = VideoList.CATALOG_ALL;
	private int curReportCatalog = ReportList.CATALOG_ALL;
	private int curPaperCatalog = PaperList.CATALOG_ALL;

	private PullToRefreshListView lvNews;
	private PullToRefreshListView lvVideo;
	private PullToRefreshListView lvReport;
	private PullToRefreshListView lvPaper;
	private ListView lvMore;

	private ListViewNewsAdapter lvNewsAdapter;
	private ListViewVideoAdapter lvVideoAdapter;
	private ListViewReportAdapter lvReportAdapter;
	private ListViewPaperAdapter lvPaperAdapter;

	private List<News> lvNewsData = new ArrayList<News>();
	private List<Video> lvVideoData = new ArrayList<Video>();
	private List<Report> lvReportData = new ArrayList<Report>();
	private List<Paper> lvPaperData = new ArrayList<Paper>();
	private ArrayList<HashMap<String, Object>> alMoreData = new ArrayList<HashMap<String, Object>>(); 

	private Handler lvNewsHandler;
	private Handler lvVideoHandler;
	private Handler lvReportHandler;
	private Handler lvPaperHandler;

	private int lvNewsLastTime;
	private int lvVideoLastTime;
	private int lvReportLastTime;
	private int lvPaperLastTime;
	
	private RadioButton fbNews;
	private RadioButton fbVideo;
	private RadioButton fbReport;
	private RadioButton fbPaper;
	private RadioButton fbSetting;

	private View lvNews_footer;
	private View lvVideo_footer;
	private View lvReport_footer;
	private View lvPaper_footer;

	private TextView lvNews_foot_more;
	private TextView lvVideo_foot_more;
	private TextView lvReport_foot_more;
	private TextView lvPaper_foot_more;

	private ProgressBar lvNews_foot_progress;
	private ProgressBar lvVideo_foot_progress;
	private ProgressBar lvReport_foot_progress;
	private ProgressBar lvPaper_foot_progress;

	public static BadgeView bv_active;

	private AppContext appContext;// 全局Context
	
	private DoubleClickExitHelper mDoubleClickExitHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mDoubleClickExitHelper = new DoubleClickExitHelper(this);
		
		appContext = (AppContext) getApplication();
		// 网络连接判断
		if (!appContext.isNetworkConnected())
			UIHelper.ToastMessage(this, R.string.network_not_connected);
		
		this.initHeadView();
		this.initFootBar();
		this.initPageScroll();
		this.initBadgeView();
		this.initFrameListView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mViewCount == 0)
			mViewCount = 4;
		if (mCurSel == 0 && !fbNews.isChecked()) {
			fbNews.setChecked(true);
			fbVideo.setChecked(false);
			fbReport.setChecked(false);
			fbPaper.setChecked(false);
		}
		// 读取左右滑动配置
		mScrollLayout.setIsScroll(appContext.isScroll());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * 初始化所有ListView
	 */
	private void initFrameListView() {
		// 初始化listview控件
		this.initNewsListView();
		this.initVideoListView();
		this.initReportListView();
		this.initPaperListView();
		// 加载listview数据
		this.initFrameListViewData();
	}

	/**
	 * 初始化所有ListView数据
	 */
	private void initFrameListViewData() {
		// 初始化Handler
		lvNewsHandler = this.getLvHandler(lvNews, lvNewsAdapter,
				lvNews_foot_more, lvNews_foot_progress, AppContext.PAGE_SIZE);
		lvVideoHandler = this.getLvHandler(lvVideo, lvVideoAdapter,
				lvVideo_foot_more, lvVideo_foot_progress, AppContext.PAGE_SIZE);
		lvReportHandler = this.getLvHandler(lvReport, lvReportAdapter,
				lvReport_foot_more, lvReport_foot_progress, AppContext.PAGE_SIZE);
		lvPaperHandler = this.getLvHandler(lvPaper, lvPaperAdapter,
				lvPaper_foot_more, lvPaper_foot_progress, AppContext.PAGE_SIZE);

		// 加载资讯数据
		if (lvNewsData.isEmpty()) {
			loadLvNewsData(curNewsCatalog, 0, lvNewsHandler,
					UIHelper.LISTVIEW_ACTION_INIT);
		}
	}

	/**
	 * 初始化新闻列表
	 */
	private void initNewsListView() {
		lvNewsAdapter = new ListViewNewsAdapter(this, lvNewsData,
				R.layout.news_listitem);
		lvNews_footer = getLayoutInflater().inflate(R.layout.listview_footer,
				null);
		lvNews_foot_more = (TextView) lvNews_footer
				.findViewById(R.id.listview_foot_more);
		lvNews_foot_progress = (ProgressBar) lvNews_footer
				.findViewById(R.id.listview_foot_progress);
		lvNews = (PullToRefreshListView) findViewById(R.id.frame_listview_news);
		lvNews.addFooterView(lvNews_footer);// 添加底部视图 必须在setAdapter前
		lvNews.setAdapter(lvNewsAdapter);
		lvNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvNews_footer)
					return;

				News news = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					news = (News) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.news_listitem_title);
					news = (News) tv.getTag();
				}
				if (news == null)
					return;

				// 跳转到新闻详情
				UIHelper.showNewsDetail(view.getContext(), news.getId(), news.getTitle(), news.getUrl());
			}
		});
		lvNews.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvNews.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvNewsData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvNews_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(lvNews.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					lvNews.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvNews_foot_more.setText(R.string.load_ing);
					lvNews_foot_progress.setVisibility(View.VISIBLE);

					loadLvNewsData(curNewsCatalog, lvNewsLastTime, lvNewsHandler,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lvNews.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		lvNews.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvNewsData(curNewsCatalog, 0, lvNewsHandler,
						UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
	}
	
	private void initVideoListView() {
		lvVideoAdapter = new ListViewVideoAdapter(this, lvVideoData,
				R.layout.video_listitem);
		lvVideo_footer = getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		lvVideo_foot_more = (TextView) lvVideo_footer
				.findViewById(R.id.listview_foot_more);
		lvVideo_foot_progress = (ProgressBar) lvVideo_footer
				.findViewById(R.id.listview_foot_progress);
		lvVideo = (PullToRefreshListView) findViewById(R.id.frame_listview_video);
		lvVideo.addFooterView(lvVideo_footer);// 添加底部视图 必须在setAdapter前
		lvVideo.setAdapter(lvVideoAdapter);
		lvVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 点击头部、底部栏无效
						if (position == 0 || view == lvVideo_footer)
							return;

						Video video = null;
						// 判断是否是TextView
						if (view instanceof TextView) {
							video = (Video) view.getTag();
						} else {
							TextView tv = (TextView) view
									.findViewById(R.id.video_listitem_title);
							video = (Video) tv.getTag();
						}
						if (video == null)
							return;

						// 跳转到问答详情
						UIHelper.showVideoDetail(view.getContext(), video.getId(), video.getTitle(), video.getUrl());
					}
				});
		lvVideo.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvVideo.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvVideoData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvVideo_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(lvVideo.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					lvVideo.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvVideo_foot_more.setText(R.string.load_ing);
					lvVideo_foot_progress.setVisibility(View.VISIBLE);

					loadLvVideoData(curVideoCatalog, lvVideoLastTime,lvVideoHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
							
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lvVideo.onScroll(view, firstVisibleItem, visibleItemCount,totalItemCount);
						
			}
		});
		lvVideo.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {
						loadLvVideoData(curVideoCatalog, 0,lvVideoHandler,UIHelper.LISTVIEW_ACTION_REFRESH);
								
					}
				});
	}
	
	private void initReportListView() {
		lvReportAdapter = new ListViewReportAdapter(this, lvReportData,R.layout.report_listitem);
		lvReport_footer = getLayoutInflater().inflate(R.layout.listview_footer,null);
		lvReport_foot_more = (TextView) lvReport_footer.findViewById(R.id.listview_foot_more);
		lvReport_foot_progress = (ProgressBar) lvReport_footer.findViewById(R.id.listview_foot_progress);
		lvReport = (PullToRefreshListView) findViewById(R.id.frame_listview_report);
		lvReport.addFooterView(lvReport_footer);// 添加底部视图 必须在setAdapter前
		lvReport.setAdapter(lvReportAdapter);
		lvReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvReport_footer)
					return;

				Report report = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					report = (Report) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.report_listitem_title);
					report = (Report) tv.getTag();
				}
				if (report == null)
					return;   			
				// 跳转到动弹详情&评论页面
				UIHelper.showReportDetail(view.getContext(), report.getId(), report.getTitle(), report.getFileUrl());
			}
		});
		lvReport.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvReport.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvReportData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvReport_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(lvReport.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					lvReport.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvReport_foot_more.setText(R.string.load_ing);
					lvReport_foot_progress.setVisibility(View.VISIBLE);

					loadLvReportData(curReportCatalog, lvReportLastTime, lvReportHandler,UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lvReport.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		lvReport.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvReportData(curReportCatalog, 0, lvReportHandler,
						UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
	}
	
	private void initPaperListView() {
		lvPaperAdapter = new ListViewPaperAdapter(this, lvPaperData,
				R.layout.paper_listitem);
		lvPaper_footer = getLayoutInflater().inflate(R.layout.report_listview_footer,
				null);
		lvPaper_foot_more = (TextView) lvPaper_footer
				.findViewById(R.id.report_listview_foot_more);
		lvPaper_foot_progress = (ProgressBar) lvPaper_footer
				.findViewById(R.id.report_listview_foot_progress);
		lvPaper = (PullToRefreshListView) findViewById(R.id.frame_listview_paper);
		lvPaper.addFooterView(lvPaper_footer);// 添加底部视图 必须在setAdapter前
		lvPaper.setAdapter(lvPaperAdapter);
		lvPaper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 点击头部、底部栏无效
				if (position == 0 || view == lvPaper_footer)
					return;

				Paper paper = null;
				// 判断是否是TextView
				if (view instanceof TextView) {
					paper = (Paper) view.getTag();
				} else {
					TextView tv = (TextView) view
							.findViewById(R.id.paper_listitem_title);
					paper = (Paper) tv.getTag();
				}
				if (paper == null)
					return;
				
				// 跳转到新闻详情
				UIHelper.showPaperDetail(view.getContext(), paper.getId(), paper.getTitle(), paper.getFileUrl());
			}
		});
		lvPaper.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvPaper.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvPaperData.isEmpty())
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvPaper_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				int lvDataState = StringUtils.toInt(lvPaper.getTag());
				if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					lvPaper.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvPaper_foot_more.setText(R.string.load_ing);
					lvPaper_foot_progress.setVisibility(View.VISIBLE);

					loadLvPaperData(curPaperCatalog, lvPaperLastTime, lvPaperHandler,
							UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lvPaper.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		lvPaper.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			public void onRefresh() {
				loadLvPaperData(curPaperCatalog, 0, lvPaperHandler,
						UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
	}

	private void initHeadView() {
		mHeadTitle = (TextView) findViewById(R.id.main_head_title);
		mHeadProgress = (ProgressBar) findViewById(R.id.main_head_progress);
		mHead_search = (ImageButton) findViewById(R.id.main_head_search);

		mHead_search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				UIHelper.showSearch(v.getContext(), mCurSel);
			}
		});
	}

	private void initFootBar() {
		fbNews = (RadioButton) findViewById(R.id.main_footbar_news);
		fbVideo = (RadioButton) findViewById(R.id.main_footbar_video);
		fbReport = (RadioButton) findViewById(R.id.main_footbar_report);
		fbPaper = (RadioButton) findViewById(R.id.main_footbar_paper);
		fbSetting = (RadioButton) findViewById(R.id.main_footbar_setting);
	}

	/**
	 * 初始化通知信息标签控件
	 */
	private void initBadgeView() {
		bv_active = new BadgeView(this, fbPaper);
		bv_active.setBackgroundResource(R.drawable.widget_count_bg);
		bv_active.setIncludeFontPadding(false);
		bv_active.setGravity(Gravity.CENTER);
		bv_active.setTextSize(8f);
		bv_active.setTextColor(Color.WHITE);
	}

	private void initPageScroll() {
		mScrollLayout = (ScrollLayout) findViewById(R.id.main_scrolllayout);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linearlayout_footer);
		mHeadTitles = getResources().getStringArray(R.array.head_titles);
		mViewCount = mScrollLayout.getChildCount();
		mButtons = new RadioButton[mViewCount];
		
		for (int i = 0; i < mViewCount; i++) {
			mButtons[i] = (RadioButton) linearLayout.getChildAt(i * 2);
			mButtons[i].setTag(i);
			mButtons[i].setChecked(false);
			mButtons[i].setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int pos = (Integer) (v.getTag());
					// 点击当前项刷新
					if (mCurSel == pos) {
						switch (pos) {
						case 0:
							lvNews.clickRefresh();
							break;
						case 1:
							lvVideo.clickRefresh();
							break;
						case 2:
							lvReport.clickRefresh();
							break;
						case 3:
							lvPaper.clickRefresh();
							break;
						}
					}
					mScrollLayout.snapToScreen(pos);
				}
			});
		}
		
		// 设置第一显示屏
		mCurSel = 0;
		mButtons[mCurSel].setChecked(true);

		mScrollLayout.SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
					public void OnViewChange(int viewIndex) {
						// 切换列表视图-如果列表数据为空：加载数据
						switch (viewIndex) {
						case 0:
							if (lvNews.getVisibility() == View.VISIBLE) {
								if (lvNewsData.isEmpty()) {
									loadLvNewsData(curNewsCatalog, 0,
											lvNewsHandler,
											UIHelper.LISTVIEW_ACTION_INIT);
								}
							}
							break;
						case 1:
							if (lvVideoData.isEmpty()) {
								loadLvVideoData(curVideoCatalog, 0,
										lvVideoHandler,
										UIHelper.LISTVIEW_ACTION_INIT);
							}
							break;
						case 2:
							if (lvReportData.isEmpty()) {
								loadLvReportData(curReportCatalog, 0,
										lvReportHandler,
										UIHelper.LISTVIEW_ACTION_INIT);
							}
							break;
						case 3:
							if (lvPaperData.isEmpty()) {
								loadLvPaperData(curPaperCatalog, 0,
										lvPaperHandler,
										UIHelper.LISTVIEW_ACTION_INIT);
							}
							break;
						case 4:
							if(alMoreData.isEmpty()) {
								lvMore = (ListView) findViewById(R.id.frame_list_more);
								loadlvMoreData();
						        SimpleAdapter listItemAdapter = new SimpleAdapter(Main.this, alMoreData, 
						            R.layout.more_listitem,     
						            new String[] {"ItemImage", "ItemTitle"},
						            new int[]{R.id.more_listitem_flag, R.id.more_listitem_title}  
						        );
						        lvMore.setAdapter(listItemAdapter);
						        lvMore.setOnItemClickListener(new OnItemClickListener() {
						            @Override  
						            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
						                    long arg3) {  
						                switch(arg2){
						                	case 0:
						                		UIHelper.showAbout(Main.this);
						                		break;
						                	case 1:
						                		UIHelper.showAbout(Main.this);
						                		break;
						                	case 2:
						                		UIHelper.showAbout(Main.this);
						                		break;
						                }
						            }
						        }); 
							}
					        break;
						}
						setCurPoint(viewIndex);
					}
				});
	}

	private void setCurPoint(int index) {
		if (index < 0 || index > mViewCount - 1 || mCurSel == index)
			return;

		mButtons[mCurSel].setChecked(false);
		mButtons[index].setChecked(true);
		mHeadTitle.setText(mHeadTitles[index]);
		mCurSel = index;

		mHead_search.setVisibility(View.GONE);
		if (index <= 3) {
			mHead_search.setVisibility(View.VISIBLE);
		}
	}

	private Handler getLvHandler(final PullToRefreshListView lv,
			final BaseAdapter adapter, final TextView more,
			final ProgressBar progress, final int pageSize) {
		return new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 0) {
					// listview数据处理
					Notice notice = handleLvData(msg.what, msg.obj, msg.arg2,
							msg.arg1);

					if (msg.what < pageSize) {
						lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_full);
					} else if (msg.what == pageSize) {
						lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_more);
					}
				} else if (msg.what == -1) {
					// 有异常--显示加载出错 & 弹出错误消息
					lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
					more.setText(R.string.load_error);
					((AppException) msg.obj).makeToast(Main.this);
				}
				if (adapter.getCount() == 0) {
					lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
					more.setText(R.string.load_empty);
				}
				progress.setVisibility(ProgressBar.GONE);
				mHeadProgress.setVisibility(ProgressBar.GONE);
				if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
					lv.onRefreshComplete(getString(R.string.pull_to_refresh_update)
							+ new Date().toLocaleString());
					lv.setSelection(0);
				} else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
					lv.onRefreshComplete();
					lv.setSelection(0);
				}
			}
		};
	}

	private Notice handleLvData(int what, Object obj, int objtype, int actiontype) {
		Notice notice = null;
		switch (actiontype) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
		case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
			int newdata = 0;// 新加载数据-只有刷新动作才会使用到
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_NEWS:
				NewsList nlist = (NewsList) obj;
				notice = nlist.getNotice();
				lvNewsLastTime = nlist.getLastTime();
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (lvNewsData.size() > 0) {
						for (News news1 : nlist.getNewslist()) {
							boolean b = false;
							for (News news2 : lvNewsData) {
								if (news1.getId() == news2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				lvNewsData.clear();// 先清除原有数据
				lvNewsData.addAll(nlist.getNewslist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_VIDEO:
				VideoList plist = (VideoList) obj;
				notice = plist.getNotice();
				lvVideoLastTime = plist.getLastTime();
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (lvVideoData.size() > 0) {
						for (Video post1 : plist.getPostlist()) {
							boolean b = false;
							for (Video post2 : lvVideoData) {
								if (post1.getId() == post2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				lvVideoData.clear();// 先清除原有数据
				lvVideoData.addAll(plist.getPostlist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_REPORT:
				ReportList tlist = (ReportList) obj;
				notice = tlist.getNotice();
				lvReportLastTime = tlist.getLastTime();
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (lvReportData.size() > 0) {
						for (Report report1 : tlist.getReportlist()) {
							boolean b = false;
							for (Report report2 : lvReportData) {
								if (report1.getId() == report2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				lvReportData.clear();// 先清除原有数据
				lvReportData.addAll(tlist.getReportlist());
				break;
			case UIHelper.LISTVIEW_DATATYPE_PAPER:
				PaperList replist = (PaperList) obj;
				notice = replist.getNotice();
				lvPaperLastTime = replist.getLastTime();
				if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
					if (lvPaperData.size() > 0) {
						for (Paper news1 : replist.getInformlist()) {
							boolean b = false;
							for (Paper news2 : lvPaperData) {
								if (news1.getId() == news2.getId()) {
									b = true;
									break;
								}
							}
							if (!b)
								newdata++;
						}
					} else {
						newdata = what;
					}
				}
				lvPaperData.clear();// 先清除原有数据
				lvPaperData.addAll(replist.getInformlist());
				break;
			}
			if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
				// 提示新加载数据
				if (newdata > 0) {
					NewDataToast
							.makeText(
									this,
									getString(R.string.new_data_toast_message,
											newdata), appContext.isAppSound())
							.show();
				} else {
					NewDataToast.makeText(this,
							getString(R.string.new_data_toast_none), false)
							.show();
				}
			}
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_NEWS:
				NewsList list = (NewsList) obj;
				notice = list.getNotice();
				lvNewsLastTime = list.getLastTime();
				if (lvNewsData.size() > 0) {
					for (News news1 : list.getNewslist()) {
						boolean b = false;
						for (News news2 : lvNewsData) {
							if (news1.getId() == news2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							lvNewsData.add(news1);
					}
				} else {
					lvNewsData.addAll(list.getNewslist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_VIDEO:
				VideoList plist = (VideoList) obj;
				notice = plist.getNotice();
				lvVideoLastTime = plist.getLastTime();
				if (lvVideoData.size() > 0) {
					for (Video post1 : plist.getPostlist()) {
						boolean b = false;
						for (Video post2 : lvVideoData) {
							if (post1.getId() == post2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							lvVideoData.add(post1);
					}
				} else {
					lvVideoData.addAll(plist.getPostlist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_REPORT:
				ReportList tlist = (ReportList) obj;
				notice = tlist.getNotice();
				lvReportLastTime = tlist.getLastTime();
				if (lvReportData.size() > 0) {
					for (Report tweet1 : tlist.getReportlist()) {
						boolean b = false;
						for (Report tweet2 : lvReportData) {
							if (tweet1.getId() == tweet2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							lvReportData.add(tweet1);
					}
				} else {
					lvReportData.addAll(tlist.getReportlist());
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_PAPER:
				PaperList rlist = (PaperList) obj;
				notice = rlist.getNotice();
				lvPaperLastTime = rlist.getLastTime();
				if (lvPaperData.size() > 0) {
					for (Paper news1 : rlist.getInformlist()) {
						boolean b = false;
						for (Paper news2 : lvPaperData) {
							if (news1.getId() == news2.getId()) {
								b = true;
								break;
							}
						}
						if (!b)
							lvPaperData.add(news1);
					}
				} else {
					lvPaperData.addAll(rlist.getInformlist());
				}
				break;
			}
			break;
		}
		return notice;
	}

	private void loadLvNewsData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		//mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					NewsList list = appContext.getNewsList(catalog, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_NEWS;
				if (curNewsCatalog == catalog)
					handler.sendMessage(msg);
			}
		}.start();
	}
	
	private void loadLvVideoData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		//mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					VideoList list = appContext.getVideoList(catalog, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_VIDEO;
				if (curVideoCatalog == catalog)
					handler.sendMessage(msg);
			}
		}.start();
	}

	private void loadLvReportData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		//mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					ReportList list = appContext.getReportList(catalog,
							pageIndex, isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_REPORT;
				if (curReportCatalog == catalog)
					handler.sendMessage(msg);
			}
		}.start();
	}
	
	private void loadLvPaperData(final int catalog, final int pageIndex,
			final Handler handler, final int action) {
		//mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread() {
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if (action == UIHelper.LISTVIEW_ACTION_REFRESH
						|| action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {
					PaperList list = appContext.getPaperList(catalog, pageIndex,
							isRefresh);
					msg.what = list.getPageSize();
					msg.obj = list;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_PAPER;
				if (curPaperCatalog == catalog)
					handler.sendMessage(msg);
			}
		}.start();
	}
	
	private void loadlvMoreData() {
		String[] ms = getResources().getStringArray(R.array.more_items);
		for(int i = 0; i < ms.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>(); 
			if(i == 0)
				map.put("ItemImage", R.drawable.more_icon_0);
			if(i == 1)
				map.put("ItemImage", R.drawable.more_icon_1);
			if(i == 2)
				map.put("ItemImage", R.drawable.more_icon_2);
            map.put("ItemTitle", ms[i]);
            alMoreData.add(map); 
		}
	}

	/**
	 * 创建menu 停用原生菜单
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	/**
	 * 监听返回--是否退出程序
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 是否退出应用
			return mDoubleClickExitHelper.onKeyDown(keyCode, event);
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {

		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			// 展示搜索页
			UIHelper.showSearch(Main.this, mCurSel);
		} else {
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;
	}
}
