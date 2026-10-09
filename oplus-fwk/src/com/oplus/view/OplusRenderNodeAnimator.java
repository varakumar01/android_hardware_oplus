package com.oplus.view;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.View;

import java.util.ArrayList;

public class OplusRenderNodeAnimator {

    public static Animator createRtAnimator(IRtAnimationTarget target, View view) {
        if (target == null || view == null) {
            return null;
        }
        // The target stops scheduling its own frames from here on and waits
        // for doFrame() calls.
        target.setAnimationHandler();
        return new TargetAnimator(target);
    }

    public static void animateToFinalPosition(Animator animator, float finalPosition) {
        if (animator instanceof TargetAnimator) {
            ((TargetAnimator) animator).animateToFinalPosition(finalPosition);
        }
    }

    /**
     * Steps an ITarget once per display frame. There is no render-thread
     * animator to hand the target to, so the frames come from the main
     * thread's Choreographer, in the uptime milliseconds the target expects.
     */
    private static final class TargetAnimator extends Animator
            implements Choreographer.FrameCallback {

        private static final Handler sMain = new Handler(Looper.getMainLooper());

        private final ITarget mTarget;
        private boolean mPumping;

        TargetAnimator(ITarget target) {
            mTarget = target;
        }

        void animateToFinalPosition(float finalPosition) {
            onMain(() -> {
                mTarget.animateToFinalPosition(finalPosition);
                pump(true);
            });
        }

        @Override
        public void start() {
            onMain(() -> {
                mTarget.start();
                pump(true);
            });
        }

        @Override
        public void cancel() {
            onMain(() -> {
                mTarget.cancel();
                finish(true);
            });
        }

        @Override
        public void end() {
            onMain(() -> {
                mTarget.end();
                finish(false);
            });
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            if (!mPumping) {
                return;
            }
            boolean done;
            try {
                done = mTarget.doFrame(frameTimeNanos / 1000000L);
            } catch (RuntimeException e) {
                done = true;
            }
            if (done || !mTarget.isRunning()) {
                finish(false);
            } else {
                Choreographer.getInstance().postFrameCallback(this);
            }
        }

        private void pump(boolean notify) {
            if (mPumping || !mTarget.isRunning()) {
                return;
            }
            mPumping = true;
            if (notify) {
                for (AnimatorListener l : listeners()) {
                    l.onAnimationStart(this);
                }
            }
            Choreographer.getInstance().postFrameCallback(this);
        }

        private void finish(boolean cancelled) {
            if (!mPumping) {
                return;
            }
            mPumping = false;
            Choreographer.getInstance().removeFrameCallback(this);
            for (AnimatorListener l : listeners()) {
                if (cancelled) {
                    l.onAnimationCancel(this);
                }
                l.onAnimationEnd(this);
            }
        }

        private ArrayList<AnimatorListener> listeners() {
            ArrayList<AnimatorListener> l = getListeners();
            return l == null ? new ArrayList<>() : new ArrayList<>(l);
        }

        private static void onMain(Runnable r) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                r.run();
            } else {
                sMain.post(r);
            }
        }

        @Override
        public boolean isRunning() {
            return mPumping || mTarget.isRunning();
        }

        @Override
        public long getStartDelay() {
            return 0;
        }

        @Override
        public void setStartDelay(long startDelay) {}

        @Override
        public Animator setDuration(long duration) {
            return this;
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public void setInterpolator(TimeInterpolator value) {}
    }
}
