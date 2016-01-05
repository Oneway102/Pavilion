package org.bitwin.pavilion;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LockerService extends Service {

    // We may want to re-enable it once LockerService is exited.
    private KeyguardManager.KeyguardLock mSysKeyguardLock;

    private WindowManager mWindowManager;
    private static WindowManager.LayoutParams LOCKER_VIEW_LAYOUT_PARAMS =
            new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.RGBA_8888
    );

    private ViewGroup mLockerView;

    @Override
    public void onCreate() {
        super.onCreate();

        // Disable system's keyguard.
        KeyguardManager keyguardManager =
                (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        mSysKeyguardLock = keyguardManager.newKeyguardLock("pavilion_kl");
        mSysKeyguardLock.disableKeyguard();

        // Register to listen to screen_on event.
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, intentFilter);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        initLayout();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setWakeLockEnabled(false);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public boolean wasScreenOn=true;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Log.d("Launcher", "action is " + action);

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                wasScreenOn = false;
                lock();
                setWakeLockEnabled(true);
            }else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                wasScreenOn = true;
            }else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                lock();
                setWakeLockEnabled(true);
            }
        }
    };

    /**
     * Init lock screen layout.
     */
    private void initLayout() {
        LOCKER_VIEW_LAYOUT_PARAMS.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLockerView = new RelativeLayout(this);
        mLockerView.setBackgroundColor(getResources().getColor(R.color.screen_locker_bg));
        ImageView logo = new ImageView(this);
        logo.setImageDrawable(getResources().getDrawable(R.drawable.screen_locker_fg));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.gravity = Gravity.CENTER;
        mLockerView.addView(logo, layoutParams);
        mLockerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWakeLockEnabled(false);
                unlock();
            }
        });
    }

    private void lock() {
        if (mLockerView.getParent() != null) return;
        mWindowManager.addView(mLockerView, LOCKER_VIEW_LAYOUT_PARAMS);
    }

    private void unlock() {
        mWindowManager.removeView(mLockerView);
    }

    /**
     * Enable/disable wake lock.
     */
    private void setWakeLockEnabled(boolean enabled) {
        if (enabled) {
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "pavilion_wl");

            }
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }
        } else {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }
    private PowerManager.WakeLock mWakeLock;
}