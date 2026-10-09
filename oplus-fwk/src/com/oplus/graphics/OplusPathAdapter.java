package com.oplus.graphics;

import android.graphics.Path;
import android.graphics.RectF;

public class OplusPathAdapter {

    private final Path mPath;

    public OplusPathAdapter(Path path, int type) {
        mPath = path;
    }

    public void addSmoothRoundRect(float left, float top, float right, float bottom, float rx,
            float ry, Path.Direction dir) {
        mPath.addRoundRect(left, top, right, bottom, rx, ry, dir);
    }

    public void addSmoothRoundRect(RectF rect, float rx, float ry, float weight,
            Path.Direction dir) {
        mPath.addRoundRect(rect, rx, ry, dir);
    }

    public void addSmoothRoundRect(RectF rect, float rx, float ry, Path.Direction dir) {
        mPath.addRoundRect(rect, rx, ry, dir);
    }

    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir) {
        mPath.addRoundRect(rect, radii, dir);
    }

    public void addSmoothRoundRect(RectF rect, float[] radii, Path.Direction dir, float weight) {
        mPath.addRoundRect(rect, radii, dir);
    }
}
