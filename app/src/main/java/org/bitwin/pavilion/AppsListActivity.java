package org.bitwin.pavilion;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends Activity {

    private PackageManager mPackageManager;
    private List<AppInfo> mAppsList;
    private ListView mListView;
    private boolean mShowAll;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.apps_list);

        mShowAll = getIntent().getBooleanExtra("showAll", false);

        mPackageManager = getPackageManager();

        loadApps();
        loadListView();
        setClickListener();
    }

    private void loadApps() {
        // We may want to avoid loading the app list each time.
        if (mAppsList != null && mAppsList.size() > 0) {
            //return;
        }

        mAppsList = new ArrayList<AppInfo>();
        AppGuard appGuard = AppGuard.getInstance();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        for (ResolveInfo ri:availableActivities) {
            if (!mShowAll && !appGuard.isAppVisible(ri.activityInfo.packageName)) {
                continue;
            }
            AppInfo app = new AppInfo();
            app.mLabel = ri.loadLabel(mPackageManager);
            app.mPackageName = ri.activityInfo.packageName;
            app.mIcon = ri.activityInfo.loadIcon(mPackageManager);
            mAppsList.add(app);
        }
    }

    // Load the app list view.
    private void loadListView(){
        mListView = (ListView)findViewById(R.id.apps_list);

        ArrayAdapter<AppInfo> adapter = new ArrayAdapter<AppInfo>(this,
                R.layout.apps_list_item,
                mAppsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.apps_list_item, null);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.item_app_icon);
                appIcon.setImageDrawable(mAppsList.get(position).mIcon);

                TextView appLabel = (TextView)convertView.findViewById(R.id.item_app_label);
                appLabel.setText(mAppsList.get(position).mLabel);

                TextView appName = (TextView)convertView.findViewById(R.id.item_app_name);
                appName.setText(mAppsList.get(position).mPackageName);

                return convertView;
            }
        };

        mListView.setAdapter(adapter);
    }

    private void setClickListener(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                String packageName = mAppsList.get(pos).mPackageName.toString();
                Intent i = mPackageManager.getLaunchIntentForPackage(packageName);
                AppsListActivity.this.startActivity(i);
            }
        });
    }

    /*
     * The app info.
     */
    static class AppInfo {
        CharSequence mLabel;
        CharSequence mPackageName;
        Drawable mIcon;
    }
}