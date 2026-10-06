package com.oplus.wrapper.graphics;

public class HardwareRenderer {

    private HardwareRenderer() {}

    public interface FrameDrawingCallback {
        void onFrameDraw(long frame);
    }
}
