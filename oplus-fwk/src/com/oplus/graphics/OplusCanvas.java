package com.oplus.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class OplusCanvas {

    private final Canvas mCanvas;

    public OplusCanvas(Canvas canvas) {
        mCanvas = canvas;
    }

    public void drawSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Paint paint, float weight) {
        mCanvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
    }

    public void drawSmoothRoundRect(RectF rect, float rx, float ry, Paint paint, float weight) {
        mCanvas.drawRoundRect(rect, rx, ry, paint);
    }
}
