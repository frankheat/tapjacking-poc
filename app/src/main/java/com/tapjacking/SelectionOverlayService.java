package com.tapjacking;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.WindowManager;

public class SelectionOverlayService extends Service {

    private WindowManager windowManager;
    private SelectionView selectionView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        selectionView = new SelectionView(this, this::onSelectionConfirmed);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(selectionView, params);
        return START_NOT_STICKY;
    }

    private void onSelectionConfirmed(int x1, int y1, int x2, int y2) {
        int sw = selectionView.getWidth();
        int sh = selectionView.getHeight();

        // TOP
        startOverlay(0, 0, sw, y1);
        // BOTTOM
        startOverlay(0, y2, sw, sh - y2);
        // LEFT
        startOverlay(0, y1, x1, y2 - y1);
        // RIGHT
        startOverlay(x2, y1, sw - x2, y2 - y1);

        if (selectionView != null) {
            windowManager.removeView(selectionView);
            selectionView = null;
        }
        stopSelf();
    }

    private void startOverlay(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Intent intent = new Intent(this, PartialOverlayService.class);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("width", width);
        intent.putExtra("height", height);
        startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (selectionView != null) {
            windowManager.removeView(selectionView);
            selectionView = null;
        }
    }
}
