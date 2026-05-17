package com.lockapp;

import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LockScreenActivity extends AppCompatActivity {

    TextView tvTime, tvDate, tvPassHint, tvMessage;
    LinearLayout layoutLock, layoutPin;
    View[] pinDots = new View[4];
    View swipeArrow;

    StringBuilder currentPin = new StringBuilder();
    Timer clockTimer;
    Handler handler = new Handler(Looper.getMainLooper());
    boolean isUnlocked = false;
    float touchDownY;
    boolean pinVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(R.layout.activity_lock_screen);
        tvTime     = findViewById(R.id.tvTime);
        tvDate     = findViewById(R.id.tvDate);
        tvPassHint = findViewById(R.id.tvPassHint);
        tvMessage  = findViewById(R.id.tvMessage);
        layoutLock = findViewById(R.id.layoutLock);
        layoutPin  = findViewById(R.id.layoutPin);
        swipeArrow = findViewById(R.id.swipeArrow);
        pinDots[0] = findViewById(R.id.dot0);
        pinDots[1] = findViewById(R.id.dot1);
        pinDots[2] = findViewById(R.id.dot2);
        pinDots[3] = findViewById(R.id.dot3);
        startClock();
        setupSwipe();
    }

    void startClock() {
        clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                handler.post(() -> {
                    Date now = new Date();
                    tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
                    tvDate.setText(new SimpleDateFormat("EEEE, dd MMMM", new Locale("vi","VN")).format(now));
                    tvPassHint.setText(new SimpleDateFormat("HHmm", Locale.getDefault()).format(now));
                });
            }
        }, 0, 1000);
    }

    void setupSwipe() {
        View root = findViewById(R.id.rootLayout);
        root.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownY = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                    float dy = touchDownY - event.getY();
                    if (!pinVisible && dy > 100) showPinPanel();
                    else if (pinVisible && dy < -100) hidePinPanel();
                    return true;
            }
            return false;
        });
    }

    void showPinPanel() {
        pinVisible = true;
        layoutPin.setVisibility(View.VISIBLE);
        swipeArrow.setVisibility(View.GONE);
        layoutPin.setTranslationY(800f);
        layoutPin.setAlpha(0f);
        layoutPin.animate().translationY(0f).alpha(1f).setDuration(380)
            .setInterpolator(new OvershootInterpolator(0.7f)).start();
        layoutLock.animate().translationY(-90f).scaleX(0.82f).scaleY(0.82f).setDuration(300).start();
    }

    void hidePinPanel() {
        pinVisible = false;
        currentPin.setLength(0);
        updateDots();
        tvMessage.setVisibility(View.GONE);
        layoutPin.animate().translationY(800f).alpha(0f).setDuration(280)
            .withEndAction(() -> {
                layoutPin.setVisibility(View.GONE);
                swipeArrow.setVisibility(View.VISIBLE);
            }).start();
        layoutLock.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(280).start();
    }

    public void onKeyPress(View v) {
        if (isUnlocked) return;
        Object tag = v.getTag();
        if (tag == null) return;
        String key = tag.toString();
        if (currentPin.length() < 4) {
            currentPin.append(key);
            updateDots();
            vibrate(25);
            if (currentPin.length() == 4) handler.postDelayed(this::checkPin, 150);
        }
    }

    public void onDelete(View v) {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updateDots();
            vibrate(15);
        }
    }

    void updateDots() {
        for (int i = 0; i < 4; i++) {
            pinDots[i].setBackgroundResource(i < currentPin.length()
                    ? R.drawable.dot_filled : R.drawable.dot_empty);
        }
    }

    void checkPin() {
        String correct = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date());
        if (currentPin.toString().equals(correct)) {
            isUnlocked = true;
            vibrate(80);
            for (View d : pinDots) d.setBackgroundResource(R.drawable.dot_filled);
            showMessage("✓  ĐÃ MỞ KHÓA", android.graphics.Color.parseColor("#00FFB0"));
            handler.postDelayed(this::unlockAndFinish, 600);
        } else {
            vibrate(350);
            for (View d : pinDots) d.setBackgroundResource(R.drawable.dot_error);
            ObjectAnimator.ofFloat(findViewById(R.id.pinRow), "translationX",
                0f,-20f,20f,-15f,15f,-8f,8f,0f).setDuration(420).start();
            showMessage("✗  Sai! Mật khẩu đúng: " + correct,
                    android.graphics.Color.parseColor("#FF4466"));
            handler.postDelayed(() -> { currentPin.setLength(0); updateDots(); }, 700);
        }
    }

    void unlockAndFinish() {
        if (clockTimer != null) clockTimer.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        }
        finish();
    }

    void showMessage(String msg, int color) {
        tvMessage.setText(msg);
        tvMessage.setTextColor(color);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setAlpha(0f);
        tvMessage.animate().alpha(1f).setDuration(200).start();
        handler.postDelayed(() ->
            tvMessage.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> tvMessage.setVisibility(View.GONE)).start(), 2500);
    }

    void vibrate(long ms) {
        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vib != null && vib.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            else vib.vibrate(ms);
        }
    }

    @Override public void onBackPressed() {}

    @Override protected void onDestroy() {
        super.onDestroy();
        if (clockTimer != null) clockTimer.cancel();
    }

    @Override protected void onPause() {
        super.onPause();
        if (!isUnlocked) {
            Intent i = new Intent(this, LockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }
    }
}
