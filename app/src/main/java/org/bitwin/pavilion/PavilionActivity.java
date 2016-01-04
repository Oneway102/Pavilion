package org.bitwin.pavilion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.view.KeyEventCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.widget.CompoundButton.OnCheckedChangeListener;

public class PavilionActivity extends Activity {

    private static final String EXIT_PASSWORD = "490237";
    private CheckBox mCheckBox;
    private ImageButton mExitButton;
    private AlertDialog mExitDialog;
    private EditText mExitPassword;
    private AlertDialog mShowAllAppsDialog;
    private EditText mShowAppAppsPassword;
    //private HomeKeyLocker mHomeKeyLocker;
    private ScreenLocker mScreenLocker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock screen orientation if needed.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        ViewGroup container = (ViewGroup) findViewById(R.id.main_container);

        mCheckBox = (CheckBox) findViewById(R.id.show_all);
        mCheckBox.setVisibility(View.VISIBLE);
        mCheckBox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mCheckBox.isChecked()) {
                    confirmShowAllApps();
                }
            }
        });

        mExitButton = (ImageButton) findViewById(R.id.exit_button);
        // Enable the color if we want to show the button.
        //mExitButton.setBackgroundColor(Color.RED);
        setupExitDetector();

        //mHomeKeyLocker = new HomeKeyLocker();
        mScreenLocker = ScreenLocker.getInstanve(this);
        mScreenLocker.setTargetView(container);

        // Do not lock screen.
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setWakeLockEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mHomeKeyLocker.lock(this);
        mScreenLocker.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScreenLocker.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mHomeKeyLocker.unlock();
        setWakeLockEnabled(false);
    }

    public void showApps(View v) {
        boolean showAll = mCheckBox == null ? false : mCheckBox.isChecked();
        Intent i = new Intent(this, AppsListActivity.class);
        i.putExtra("showAll", showAll);
        startActivity(i);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Swallow BACK key.
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mScreenLocker != null) {
            mScreenLocker.onActivityTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void setupExitDetector() {
        if (mExitButton == null) return;
        // Register one of the detectors to exit.
        //registerLongPressToExit();
        registerFourfoldTapToExit();
    }

    // Detect for long-press on the EXIT button to exit.
    private void registerLongPressToExit() {
        mExitButton.setLongClickable(true);
        mExitButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                confirmExit();
                return true;
            }
        });
    }

    // Detect for fourfold tap on the EXIT button to exit.
    private long mLastClickTimestamp = 0;
    private int mClickCount = 0;
    private static long MAX_CLICK_INTERVAL = 200; // 200ms
    private void registerFourfoldTapToExit() {
        mExitButton.setClickable(true);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                if (now - mLastClickTimestamp > MAX_CLICK_INTERVAL) {
                    // reset
                    mLastClickTimestamp = now;
                    mClickCount = 1;
                    return;
                }
                mLastClickTimestamp = now;
                mClickCount++;
                if (mClickCount >= 4) {
                    mClickCount = 0;
                    confirmExit();
                }
            }
        });

    }

    private void confirmExit() {
        if (mExitDialog == null) {
            mExitPassword = new EditText(PavilionActivity.this);
            mExitDialog = new AlertDialog.Builder(this)
                    .setView(mExitPassword)
                    .setTitle(R.string.password_prompt_title)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String password = mExitPassword.getText().toString();
                            if (!EXIT_PASSWORD.equals(password)) {
                                Toast.makeText(PavilionActivity.this, R.string.incorrect_password, Toast.LENGTH_LONG).show();
                                return;
                            }
                            exit();
                        }
                    })
                    .create();
        }
        // Show the dialog.
        mExitPassword.setText("");
        mExitDialog.show();
    }

    private void confirmShowAllApps() {
        if (mShowAllAppsDialog == null) {
            mShowAppAppsPassword = new EditText(PavilionActivity.this);
            mShowAllAppsDialog = new AlertDialog.Builder(this)
                    .setView(mShowAppAppsPassword)
                    .setTitle(R.string.password_prompt_title)
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCheckBox.setChecked(false);
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String password = mShowAppAppsPassword.getText().toString();
                            if (!EXIT_PASSWORD.equals(password)) {
                                Toast.makeText(PavilionActivity.this, R.string.incorrect_password, Toast.LENGTH_LONG).show();
                                mCheckBox.setChecked(false);
                            }
                        }
                    })
                    .create();
        }
        // Show the dialog.
        mShowAppAppsPassword.setText("");
        mShowAllAppsDialog.show();
    }

    /*
     * Just show app list so that we can exit Pavilion in system Settings,
     * in case it has been set as the default launcher.
     * TODO: There might be a better approach to stop Pavilion and switch
     * back to the stock launcher.
     */
    private void exit() {
        //PavilionActivity.this.finish();
        mCheckBox.setChecked(true);
        showApps(mCheckBox);
        Toast.makeText(PavilionActivity.this, R.string.exit_hint, Toast.LENGTH_LONG).show();
    }

    /**
     * Ask for wake lock to avoid screen off.
     */
    private void setWakeLockEnabled(boolean enabled) {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        if (enabled) {
            PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "pavilion_wl");
            mWakeLock.acquire();
        } else {
            //
        }
    }
    private PowerManager.WakeLock mWakeLock;
}

