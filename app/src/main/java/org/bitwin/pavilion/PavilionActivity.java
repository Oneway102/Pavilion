package org.bitwin.pavilion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.KeyEventCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class PavilionActivity extends Activity {

    private static final String EXIT_PASSWORD = "490237";
    private EditText mPasswordInput;
    private CheckBox mCheckBox;
    private ImageButton mExitButton;
    private AlertDialog mExitDialog;
    //private HomeKeyLocker mHomeKeyLocker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lock screen orientation if needed.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main);
        mCheckBox = (CheckBox) findViewById(R.id.show_all);
        mCheckBox.setVisibility(View.GONE);
        mExitButton = (ImageButton) findViewById(R.id.exit_button);
        // Enable the color if we want to show the button.
        //mExitButton.setBackgroundColor(Color.RED);
        setupExitDetector();
        //mHomeKeyLocker = new HomeKeyLocker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mHomeKeyLocker.lock(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mHomeKeyLocker.unlock();
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
            mPasswordInput = new EditText(PavilionActivity.this);
            mExitDialog = new AlertDialog.Builder(this)
                    .setView(mPasswordInput)
                    .setTitle(R.string.exit_prompt_title)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String password = mPasswordInput.getText().toString();
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
        mPasswordInput.setText("");
        mExitDialog.show();
    }

    private void exit() {
        PavilionActivity.this.finish();
    }
}