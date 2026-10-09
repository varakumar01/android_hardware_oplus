package com.oplus.app;

import android.content.Context;

public class OplusHansFreezeManager {

    private static OplusHansFreezeManager sInstance;

    public static OplusHansFreezeManager getInstance() {
        if (sInstance == null) {
            sInstance = new OplusHansFreezeManager();
        }
        return sInstance;
    }

    public int requestFastFreeze(Context context, int type, String reason) {
        return -1;
    }
}
