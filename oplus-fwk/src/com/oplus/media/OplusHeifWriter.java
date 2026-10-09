package com.oplus.media;

import java.io.FileDescriptor;

/**
 * Java side of liboplusheifwriter.so, which registers these four native
 * methods on this class name in its JNI_OnLoad.
 */
public class OplusHeifWriter {

    public static final int COLOR_FMT_YUV420Planar = 0;
    public static final int COLOR_FMT_P010 = 1;
    public static final int COLOR_FMT_RGBA8888 = 2;
    public static final int COLOR_FMT_NV12 = 3;
    public static final int COLOR_FMT_NV21 = 4;
    public static final int COLOR_FMT_MAX = 5;

    static {
        System.loadLibrary("oplusheifwriter");
    }

    private long mNativeObject = nativeSetup();

    private static native long nativeSetup();

    private static native long nativeCreate(long nativeObject, int width, int height,
            int strideWidth, int strideHeight, int fmt, int quality, int rotation);

    private static native long nativeProcessHeicPhotoFrame(long nativeObject, byte[] yuvBuffer,
            byte[] exifData, FileDescriptor fd);

    private static native void nativeDestory(long nativeObject);

    public boolean createPrimaryImage(int width, int height, int strideWidth, int strideHeight,
            int fmt, int quality, int rotation) {
        if (quality <= 0 || quality > 100) {
            throw new IllegalArgumentException("quality range error");
        }
        if (width <= 0 || height <= 0 || strideWidth <= 0 || strideHeight <= 0
                || fmt < 0 || fmt >= COLOR_FMT_MAX) {
            return false;
        }
        return nativeCreate(mNativeObject, width, height, strideWidth, strideHeight, fmt,
                quality, rotation) >= 0;
    }

    public boolean processPrimaryImage(byte[] yuvBuffer, byte[] exifData, FileDescriptor fd) {
        return nativeProcessHeicPhotoFrame(mNativeObject, yuvBuffer, exifData, fd) >= 0;
    }

    public void destory() {
        if (mNativeObject != 0) {
            nativeDestory(mNativeObject);
            mNativeObject = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            destory();
        } finally {
            super.finalize();
        }
    }
}
