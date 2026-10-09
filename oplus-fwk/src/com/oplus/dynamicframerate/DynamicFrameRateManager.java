package com.oplus.dynamicframerate;

import android.os.Bundle;
import android.view.SurfaceControl;

public class DynamicFrameRateManager {

    public static int getDynamicFrameRateType() {
        return 0;
    }

    public static int getSuggestFrameRate(float velocity, int type) {
        return 0;
    }

    public static boolean setFrameRate(Object target, int frameRate, int type, Bundle extras) {
        return false;
    }

    public static boolean isTypeEnable(int type) {
        return false;
    }

    public static boolean setFrameRateNoContext(Object target,
            SurfaceControl.Transaction transaction, int frameRate, int type, Bundle extras) {
        return false;
    }
}
