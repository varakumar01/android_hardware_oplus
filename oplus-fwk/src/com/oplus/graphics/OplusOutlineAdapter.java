package com.oplus.graphics;

import android.graphics.Outline;
import android.graphics.Rect;

public class OplusOutlineAdapter {

    private final Outline mOutline;

    public OplusOutlineAdapter(Outline outline, int type) {
        mOutline = outline;
    }

    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius) {
        mOutline.setRoundRect(left, top, right, bottom, radius);
    }

    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius,
            float weight) {
        mOutline.setRoundRect(left, top, right, bottom, radius);
    }

    public void setSmoothRoundRect(Rect rect, float radius) {
        mOutline.setRoundRect(rect, radius);
    }

    public void setSmoothRoundRect(Rect rect, float radius, float weight) {
        mOutline.setRoundRect(rect, radius);
    }
}
