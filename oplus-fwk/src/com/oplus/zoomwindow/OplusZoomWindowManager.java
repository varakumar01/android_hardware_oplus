package com.oplus.zoomwindow;

import android.content.Intent;
import android.os.Bundle;

public class OplusZoomWindowManager {

    public static OplusZoomWindowManager sOplusZoomWindowManager = null;

    public static OplusZoomWindowManager getInstance() {
        if (sOplusZoomWindowManager == null) {
            sOplusZoomWindowManager = new OplusZoomWindowManager();
        }
        return sOplusZoomWindowManager;
    }

    public boolean registerZoomWindowObserver(IOplusZoomWindowObserver observer) {
        return false;
    }

    public boolean unregisterZoomWindowObserver(IOplusZoomWindowObserver observer) {
        return false;
    }

    public OplusZoomWindowInfo getCurrentZoomWindowState() {
        return new OplusZoomWindowInfo();
    }

    public boolean isSupportZoomMode(String target, int userId, String callPkg, Bundle extension) {
        return false;
    }

    public int startZoomWindow(Intent intent, Bundle options, int userId, String callPkg) {
        return -1;
    }
}
