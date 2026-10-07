package com.oplus.media;

public class OplusAudioManager {

    private static OplusAudioManager sInstance;

    public static OplusAudioManager getInstance() {
        if (sInstance == null) {
            sInstance = new OplusAudioManager();
        }
        return sInstance;
    }

    public void setRingerModeInternal(int ringerMode) {}
}
