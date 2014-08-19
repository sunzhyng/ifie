package net.ifie.app.ui;

import net.ifie.app.R;
import net.ifie.app.common.UIHelper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReportDetail extends BaseActivity {

	private ImageView mBack;
	private TextView mTitle;
	private ProgressBar mProgressbar;
	
	//private String url;
    private String title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_detail);
		// 初始化视图控件
		
		mProgressbar = (ProgressBar) findViewById(R.id.report_detail_head_progress);
		
		//url = getIntent().getStringExtra("url");
		title = getIntent().getStringExtra("title");
		
		mBack = (ImageView) findViewById(R.id.report_detail_back);
        mBack.setOnClickListener(UIHelper.finish(this));

        mTitle = (TextView) findViewById(R.id.report_detail_head_title);
        mTitle.setText(title);
        
		mProgressbar.setVisibility(View.GONE);
		
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mo.wps.cn/")); 
		        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		        intent.setAction(android.content.Intent.ACTION_VIEW); 
		        startActivity(intent);  
				
			}
		});
	}
	

}