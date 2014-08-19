package net.ifie.app.ui;

import net.ifie.app.AppManager;
import net.ifie.app.common.UIHelper;
import net.ifie.app.R;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Setting extends PreferenceActivity {

	Preference about;

	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);

		// 设置显示Preferences
		addPreferencesFromResource(R.xml.preferences);
		// 获得SharedPreferences

		ListView localListView = getListView();
		localListView.setBackgroundColor(0);
		localListView.setCacheColorHint(0);
		((ViewGroup) localListView.getParent()).removeView(localListView);
		ViewGroup localViewGroup = (ViewGroup) getLayoutInflater().inflate(
				R.layout.setting, null);
		((ViewGroup) localViewGroup.findViewById(R.id.setting_content))
				.addView(localListView, -1, -1);
		setContentView(localViewGroup);

		// 关于我们
		about = (Preference) findPreference("about");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIHelper.showAbout(Setting.this);
				return true;
			}
		});

	}

	public void back(View paramView) {
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}
}
