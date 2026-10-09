package com.oplus.view;

public interface ITarget {

    void cancel();

    boolean doFrame(long frameTime);

    void end();

    boolean isRunning();

    void start();

    default boolean doFrame(long frameTime, boolean isRtMode) {
        return doFrame(frameTime);
    }

    default void skipToEnd() {
        end();
    }

    default void animateToFinalPosition(float finalPosition) {}

    default void setAnimationHandler() {}
}
