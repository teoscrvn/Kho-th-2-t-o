package com.lockapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class LockService extends Service {

    public static boolean isRunning = false;
    private ScreenReceiver receiver;
    private static final String CHANNEL_ID = "LockServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        createNotificationChannel();
        startForeground(1, buildNotification());

        // Đăng ký lắng nghe tắt/bật màn hình
        receiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Tự khởi động lại nếu bị kill
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (receiver != null) unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Màn hình khóa thứ 2",
                NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Đang bảo vệ thiết bị của bạn");
            ch.setShowBadge(false);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔒 Màn hình khóa đang hoạt động")
            .setContentText("Mật khẩu = giờ + phút hiện tại")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }
}
