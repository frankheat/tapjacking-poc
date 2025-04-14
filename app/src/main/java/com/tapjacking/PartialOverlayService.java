package com.tapjacking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class PartialOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int heightPercent = intent.getIntExtra("height", 50);
        int widthPercent = intent.getIntExtra("width", 50);

        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();

        // Calculate the overlay dimensions in pixels based on the screen size
        int overlayHeight = (screenHeight * heightPercent) / 100;
        int overlayWidth = (screenWidth * widthPercent) / 100;

        // Inflating the layout for the overlay
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_partial_layout, null);

        // Set up the button to close the overlay
        Button closeButton = overlayView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> stopSelf());

        // Set up the overlay text
        TextView overlayText = overlayView.findViewById(R.id.overlayText);
        overlayText.setText("Partial Overlay");

        // Define layout parameters
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                overlayWidth, overlayHeight,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;

        // Show the overlay
        windowManager.addView(overlayView, params);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
}
