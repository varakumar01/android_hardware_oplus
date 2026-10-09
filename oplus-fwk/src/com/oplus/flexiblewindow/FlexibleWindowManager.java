package com.oplus.flexiblewindow;

import android.app.Activity;

public class FlexibleWindowManager {

    private static FlexibleWindowManager sInstance;

    public static FlexibleWindowManager getInstance() {
        if (sInstance == null) {
            sInstance = new FlexibleWindowManager();
        }
        return sInstance;
    }

    public void removeEmbeddedContainerTask(int taskId, int flags) {}

    public int getFlexibleWindowState(Activity activity) {
        return 0;
    }
}
