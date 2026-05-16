package com.lockapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Switch switchLock;
    TextView tvCurrentPass, tvStatus;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchLock  = findViewById(R.id.switchLock);
        tvCurrentPass = findViewById(R.id.tvCurrentPass);
        tvStatus    = findViewById(R.id.tvStatus);

        // Hiển thị mật khẩu live
        startPassTimer();

        // Trạng thái switch theo service
        boolean running = LockService.isRunning;
        switchLock.setChecked(running);
        tvStatus.setText(running ? "🟢 Đang bảo vệ" : "🔴 Chưa bật");

        switchLock.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                // Xin quyền overlay nếu chưa có
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(this)) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(i, 100);
                    btn.setChecked(false);
                    Toast.makeText(this,
                            "Hãy cấp quyền 'Hiển thị trên các app khác' rồi bật lại!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                startLockService();
            } else {
                stopLockService();
            }
        });

        // Nút test thử màn hình khóa
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            Intent i = new Intent(this, LockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
    }

    void startLockService() {
        Intent i = new Intent(this, LockService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(i);
        else
            startService(i);
        tvStatus.setText("🟢 Đang bảo vệ");
        Toast.makeText(this, "Màn hình khóa thứ 2 đã bật!", Toast.LENGTH_SHORT).show();
    }

    void stopLockService() {
        stopService(new Intent(this, LockService.class));
        tvStatus.setText("🔴 Chưa bật");
        Toast.makeText(this, "Đã tắt màn hình khóa thứ 2", Toast.LENGTH_SHORT).show();
    }

    void startPassTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                String pass = new SimpleDateFormat("HHmm", Locale.getDefault())
                        .format(new Date());
                runOnUiThread(() -> tvCurrentPass.setText(pass));
            }
        }, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Settings.canDrawOverlays(this)) {
                switchLock.setChecked(true);
                startLockService();
            }
        }
    }
}
