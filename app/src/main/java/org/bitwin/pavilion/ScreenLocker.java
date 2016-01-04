package org.bitwin.pavilion;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

class ScreenLocker {

    public static ScreenLocker mInstance;

    private Activity mActivity;

    private View mLockerView;
    private ViewGroup mTargetView;
    private boolean mIsLocked = false;

    private static int CHECK_INTERVAL = 60 * 1000;
    private static int MSG_ID_LOCK_TIMER = 9;
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_ID_LOCK_TIMER) return;
            ScreenLocker screenLocker = (ScreenLocker) msg.obj;
            if (screenLocker == null) return;
            screenLocker.lock();
        }
    };

    /**
     * Singleton.
     */
    public static synchronized ScreenLocker getInstanve(Activity activity) {
        if (mInstance == null) {
            mInstance = new ScreenLocker(activity);
        }
        return mInstance;
    }

    protected ScreenLocker(Activity activity) {
        mActivity = activity;
        mLockerView = mActivity.getLayoutInflater().inflate(R.layout.lock_screen, null);
    }

    /**
     * Set the target view, with which we will have the screen locker attached to.
     */
    public void setTargetView(ViewGroup targetView) {
        if (mTargetView != null) {
            mTargetView.removeView(mLockerView);
        }
        mTargetView = targetView;
    }

    /**
     * Monitor activity touch events, so that we can de- or activate screen locker.
     */
    public void onActivityTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return;
        if (mIsLocked) {
            unlock();
        }
        scheduleLock();
    }

    /**
     * Start to work.
     */
    public void start() {
        scheduleLock();
    }

    /**
     * Stop working.
     */
    public void stop() {
        mHandler.removeMessages(MSG_ID_LOCK_TIMER);
        unlock();
    }

    /**
     * Schedule the locker.
     */
    private void scheduleLock() {
        mHandler.removeMessages(MSG_ID_LOCK_TIMER);
        Message msg = Message.obtain(mHandler, MSG_ID_LOCK_TIMER, this);
        mHandler.sendMessageDelayed(msg, getNextInterval());
    }

    /**
     * Show the locker screen.
     */
    private void lock() {
        if (mTargetView == null || mLockerView == null) return;
        if (mLockerView.getParent() == mTargetView) {
            mIsLocked = true;
            return;
        }
        mTargetView.addView(mLockerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mIsLocked = true;
    }

    /**
     * Unlock.
     */
    private void unlock() {
        mIsLocked = false;
        if (mTargetView == null || mLockerView == null) return;
        mTargetView.removeView(mLockerView);
    }

    /**
     * Calculate the next timer interval.
     */
    private int getNextInterval() {
        return CHECK_INTERVAL;
    }
}