package com.oplus.view;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewRootImpl;

import com.android.internal.graphics.drawable.BackgroundBlurDrawable;
import com.oplus.graphics.OplusBlurParam;

public class ViewRootManager {

    private BackgroundBlurDrawable mBackgroundBlurDrawable;

    public ViewRootManager(View view) {
        ViewRootImpl viewRootImpl = view.getViewRootImpl();
        if (viewRootImpl != null) {
            mBackgroundBlurDrawable = viewRootImpl.createBackgroundBlurDrawable();
        }
    }

    public Drawable getBackgroundBlurDrawable() {
        return mBackgroundBlurDrawable;
    }

    public void setBlurParams(OplusBlurParam params) {}

    public void setBlurRadius(int blurRadius) {
        if (mBackgroundBlurDrawable != null) {
            mBackgroundBlurDrawable.setBlurRadius(blurRadius);
        }
    }

    public void setColor(int color) {
        if (mBackgroundBlurDrawable != null) {
            mBackgroundBlurDrawable.setColor(color);
        }
    }

    public void setCornerRadius(float cornerRadius) {
        if (mBackgroundBlurDrawable != null) {
            mBackgroundBlurDrawable.setCornerRadius(cornerRadius);
        }
    }

    public void setCornerRadius(float topLeft, float topRight, float bottomLeft,
            float bottomRight) {
        if (mBackgroundBlurDrawable != null) {
            mBackgroundBlurDrawable.setCornerRadius(topLeft, topRight, bottomLeft, bottomRight);
        }
    }
}
