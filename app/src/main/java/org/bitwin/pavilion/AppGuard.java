package org.bitwin.pavilion;

import java.util.HashMap;

class AppGuard {

    private static AppGuard mInstance;
    private HashMap<String, Integer> mAppWhiteList;

    public static synchronized AppGuard getInstance() {
        if (mInstance == null) {
            mInstance = new AppGuard();
        }
        return mInstance;
    }

    // Init all the visible apps here.
    // TODO: We may want to read it from a config file.
    private AppGuard() {
        mAppWhiteList = new HashMap<>(10);
        int count = 1;
        mAppWhiteList.put("com.android.calendar", count++);
        mAppWhiteList.put("com.android.settings", count++);
    }

    public boolean isAppVisible(String packageName) {
        if (mAppWhiteList == null) return true;
        Integer index = mAppWhiteList.get(packageName);
        if (index != null) {
            return true;
        }
        return false;
    }
}