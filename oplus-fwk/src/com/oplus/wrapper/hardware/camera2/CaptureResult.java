package com.oplus.wrapper.hardware.camera2;

import android.util.Log;

import com.oplus.wrapper.hardware.camera2.impl.CameraMetadataNative;

import java.lang.reflect.Field;

public class CaptureResult {

    private static final String TAG = "OplusCaptureResult";

    private final android.hardware.camera2.CaptureResult mCaptureResult;

    public CaptureResult(android.hardware.camera2.CaptureResult captureResult) {
        mCaptureResult = captureResult;
    }

    // AOSP has no CaptureResult.getNativeMetadata(); mResults is the object it returns in
    // the OPLUS framework (the live metadata, not a copy).
    public CameraMetadataNative getNativeMetadata() {
        try {
            Field results = android.hardware.camera2.CaptureResult.class
                    .getDeclaredField("mResults");
            results.setAccessible(true);
            Object metadata = results.get(mCaptureResult);
            if (metadata == null) {
                return null;
            }
            return new CameraMetadataNative(
                    (android.hardware.camera2.impl.CameraMetadataNative) metadata);
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "getNativeMetadata failed", e);
            return null;
        }
    }
}
