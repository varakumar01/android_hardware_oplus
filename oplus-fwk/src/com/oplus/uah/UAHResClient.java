package com.oplus.uah;

import com.oplus.uah.info.UAHEventRequest;

public class UAHResClient {

    private static UAHResClient sInstance;

    public static UAHResClient get(Class clazz) {
        if (sInstance == null) {
            sInstance = new UAHResClient();
        }
        return sInstance;
    }

    public int acquireEvent(UAHEventRequest request) {
        return -1;
    }

    public void release(int handle) {}
}
