package com.oplus.osense.eventinfo;

import android.os.Bundle;

public class OsenseConfig {

    private final int mEventType;
    private final Bundle mExtra;

    public OsenseConfig(int eventType, Bundle extra) {
        mEventType = eventType;
        mExtra = extra;
    }
}
