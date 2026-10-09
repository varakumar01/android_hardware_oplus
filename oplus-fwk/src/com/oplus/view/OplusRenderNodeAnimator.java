package com.oplus.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

public class OplusRenderNodeAnimator {

    // No render-thread animator here: the caller gets an animator that never
    // drives the target, so a start()/cancel() on it is harmless.
    public static Animator createRtAnimator(IRtAnimationTarget target, View view) {
        return new ValueAnimator();
    }

    public static void animateToFinalPosition(Animator animator, float finalPosition) {}
}
