package net.ifie.app.ui;

import net.ifie.app.common.UIHelper;
import net.ifie.app.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * 问答详情
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class VideoDetail extends BaseActivity {

	private ImageView mBack;
	private TextView mTitle;
	private VideoView mVideo;
	private MediaController mMediaCtrl;
	private ProgressBar mProgressbar;
	
    private String url;
    private String title;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail);
        
        mProgressbar = (ProgressBar) findViewById(R.id.video_detail_head_progress);
        
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        
        mBack = (ImageView) findViewById(R.id.video_detail_back);
        mBack.setOnClickListener(UIHelper.finish(this));
        
        mTitle = (TextView) findViewById(R.id.video_detail_head_title);
        mTitle.setText(title);
        
		Uri mUri = Uri.parse(url);
	    mVideo = (VideoView)findViewById(R.id.post_video);
	    mMediaCtrl = new MediaController(this){
	    	@Override
	        public void show(int timeout) {
	            super.show(0);
	        }
	    };
        mMediaCtrl.setAnchorView(mVideo);
        mVideo.setMediaController(mMediaCtrl);

	    mVideo.setVideoURI(mUri);
	    mVideo.requestFocus();
	    
	    mProgressbar.setVisibility(View.GONE);
        
    }
    
    @Override
	protected void onResume() {
    	super.onResume();
    	mVideo.start();
    }
    
}
