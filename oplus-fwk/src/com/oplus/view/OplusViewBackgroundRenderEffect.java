package com.oplus.view;

import android.graphics.RenderEffect;
import android.view.View;

public class OplusViewBackgroundRenderEffect {

    // The effect is applied to what is drawn behind the view, which is what
    // AOSP's backdrop render effect does.
    public static void setBackgroundRenderEffect(RenderEffect effect, View view) {
        if (view != null) {
            view.setBackdropRenderEffect(effect);
        }
    }
}
