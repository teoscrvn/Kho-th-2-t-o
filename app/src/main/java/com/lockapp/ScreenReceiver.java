package com.lockapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            // Màn hình vừa tắt → chờ 300ms rồi hiện lock screen
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent lockIntent = new Intent(context, LockScreenActivity.class);
                lockIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                  | Intent.FLAG_ACTIVITY_CLEAR_TOP
                  | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );
                context.startActivity(lockIntent);
            }, 300);
        }
        // ACTION_USER_PRESENT = đã qua khóa gốc, lock screen của chúng ta sẽ hiện sẵn
    }
}
