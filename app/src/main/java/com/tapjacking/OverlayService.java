package com.tapjacking;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.graphics.drawable.ColorDrawable;
import android.graphics.PixelFormat;

public class OverlayService extends Service {

    private View overlayView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Creating the overlay
        createOverlay();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createOverlay() {
        // Creating a Layout of type FrameLayout
        FrameLayout overlayLayout = new FrameLayout(this);
        overlayLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Setting the semi-transparent background
        overlayLayout.setBackground(new ColorDrawable(Color.argb(180, 0, 0, 0)));

        // Creating the TextView for the message
        TextView textView = new TextView(this);
        textView.setText("Full Overlay");
        textView.setTextColor(Color.RED);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER);

        // Adding the TextView to the overlay
        overlayLayout.addView(textView);

        // Creating settings for the WindowManager
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;

        // Get the WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.addView(overlayLayout, params);
        }

        overlayView = overlayLayout;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            // Removing the view when the service ends
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.removeView(overlayView);
            }
        }
    }
}
