package com.tapjacking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

public class SelectionView extends View {

    public interface OnConfirmedListener {
        void onConfirmed(int x1, int y1, int x2, int y2);
    }

    private final OnConfirmedListener listener;

    private final Paint bgPaint = new Paint();
    private final Paint holePaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint dotPaint = new Paint();
    private final Paint btnPaint = new Paint();
    private final Paint btnTextPaint = new Paint();
    private final Paint hintPaint = new Paint();

    private int p1x = -1, p1y = -1;
    private int p2x = -1, p2y = -1;
    private boolean firstSet = false;
    private boolean secondSet = false;

    private RectF confirmBtn = null;
    private RectF resetBtn = null;

    public SelectionView(Context context, OnConfirmedListener listener) {
        super(context);
        this.listener = listener;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initPaints();
    }

    private void initPaints() {
        bgPaint.setColor(Color.argb(150, 0, 0, 0));

        holePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        borderPaint.setColor(Color.RED);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setPathEffect(new DashPathEffect(new float[]{24, 12}, 0));

        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);

        btnPaint.setStyle(Paint.Style.FILL);

        btnTextPaint.setColor(Color.WHITE);
        btnTextPaint.setTextSize(44f);
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        btnTextPaint.setAntiAlias(true);

        hintPaint.setColor(Color.WHITE);
        hintPaint.setTextSize(44f);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setAntiAlias(true);
        hintPaint.setShadowLayer(4f, 0, 0, Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        if (firstSet && !secondSet) {
            canvas.drawCircle(p1x, p1y, 24, dotPaint);
            canvas.drawText("Tap bottom-right corner", getWidth() / 2f, getHeight() * 0.08f, hintPaint);
        }

        if (secondSet) {
            int left = Math.min(p1x, p2x);
            int top = Math.min(p1y, p2y);
            int right = Math.max(p1x, p2x);
            int bottom = Math.max(p1y, p2y);

            canvas.drawRect(left, top, right, bottom, holePaint);
            canvas.drawRect(left, top, right, bottom, borderPaint);

            canvas.drawCircle(p1x, p1y, 24, dotPaint);
            canvas.drawCircle(p2x, p2y, 24, dotPaint);

            drawButtons(canvas);
        }

        if (!firstSet) {
            canvas.drawText("Tap top-left corner", getWidth() / 2f, getHeight() * 0.08f, hintPaint);
        }
    }

    private void drawButtons(Canvas canvas) {
        float btnWidth = getWidth() * 0.35f;
        float btnHeight = 130f;
        float btnY = getHeight() - 220f;
        float margin = getWidth() * 0.075f;

        resetBtn = new RectF(margin, btnY, margin + btnWidth, btnY + btnHeight);
        btnPaint.setColor(Color.argb(230, 80, 80, 80));
        canvas.drawRoundRect(resetBtn, 24, 24, btnPaint);
        canvas.drawText("RESET", resetBtn.centerX(), resetBtn.centerY() + 16, btnTextPaint);

        float confirmX = getWidth() - margin - btnWidth;
        confirmBtn = new RectF(confirmX, btnY, confirmX + btnWidth, btnY + btnHeight);
        btnPaint.setColor(Color.argb(230, 0, 150, 0));
        canvas.drawRoundRect(confirmBtn, 24, 24, btnPaint);
        canvas.drawText("CONFIRM", confirmBtn.centerX(), confirmBtn.centerY() + 16, btnTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        float x = event.getX();
        float y = event.getY();

        if (secondSet) {
            if (confirmBtn != null && confirmBtn.contains(x, y)) {
                listener.onConfirmed(
                        Math.min(p1x, p2x), Math.min(p1y, p2y),
                        Math.max(p1x, p2x), Math.max(p1y, p2y));
                return true;
            }
            if (resetBtn != null && resetBtn.contains(x, y)) {
                reset();
                return true;
            }
        }

        if (!firstSet) {
            p1x = (int) x;
            p1y = (int) y;
            firstSet = true;
        } else if (!secondSet) {
            p2x = (int) x;
            p2y = (int) y;
            secondSet = true;
        }

        invalidate();
        return true;
    }

    private void reset() {
        firstSet = false;
        secondSet = false;
        confirmBtn = null;
        resetBtn = null;
        invalidate();
    }
}
