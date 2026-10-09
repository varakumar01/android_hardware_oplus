package com.oplus.animation;

import android.os.Looper;
import android.view.View;

public class OplusAsyncAnimatorUtils {

    // There is no render-thread property setter here; the value is set on the
    // view's own thread instead.
    private static boolean apply(View view, Runnable setter) {
        if (view == null) {
            return false;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setter.run();
        } else {
            view.post(setter);
        }
        return true;
    }

    public static boolean setAlpha(View view, float value) {
        return apply(view, () -> view.setAlpha(value));
    }

    public static boolean setRotation(View view, float value) {
        return apply(view, () -> view.setRotation(value));
    }

    public static boolean setScaleX(View view, float value) {
        return apply(view, () -> view.setScaleX(value));
    }

    public static boolean setScaleY(View view, float value) {
        return apply(view, () -> view.setScaleY(value));
    }

    public static boolean setTranslationX(View view, float value) {
        return apply(view, () -> view.setTranslationX(value));
    }

    public static boolean setTranslationY(View view, float value) {
        return apply(view, () -> view.setTranslationY(value));
    }
}
