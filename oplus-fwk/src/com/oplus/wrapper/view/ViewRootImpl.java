package com.oplus.wrapper.view;

import android.view.SurfaceControl;

import com.oplus.wrapper.graphics.HardwareRenderer;

public class ViewRootImpl {

    private final android.view.ViewRootImpl mViewRootImpl;

    ViewRootImpl(android.view.ViewRootImpl viewRootImpl) {
        mViewRootImpl = viewRootImpl;
    }

    public SurfaceControl getSurfaceControl() {
        return mViewRootImpl.getSurfaceControl();
    }

    public void registerRtFrameCallback(HardwareRenderer.FrameDrawingCallback callback) {
        mViewRootImpl.registerRtFrameCallback(callback::onFrameDraw);
    }
}
