package com.oplus.osense;

import android.content.Context;
import android.os.Bundle;

import com.oplus.osense.eventinfo.EventConfig;
import com.oplus.osense.eventinfo.OsenseEventCallback;
import com.oplus.osense.task.BgRunningCallback;

public class OsenseResEventClient {

    private static OsenseResEventClient sInstance;

    public static OsenseResEventClient getInstance() {
        if (sInstance == null) {
            sInstance = new OsenseResEventClient();
        }
        return sInstance;
    }

    public int registerEventCallback(OsenseEventCallback callback, EventConfig config) {
        return -1;
    }

    public int unregisterEventCallback(OsenseEventCallback callback) {
        return -1;
    }

    public void requestSceneAction(Bundle bundle) {}

    public void startBackgroundRunning(Context context, int taskType,
            BgRunningCallback callback) {}

    public boolean stopBackgroundRunning(Context context, int taskType) {
        return false;
    }
}
