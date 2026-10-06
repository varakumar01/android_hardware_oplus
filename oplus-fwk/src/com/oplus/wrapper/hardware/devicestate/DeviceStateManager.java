package com.oplus.wrapper.hardware.devicestate;

import android.content.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class DeviceStateManager {

    private final android.hardware.devicestate.DeviceStateManager mDeviceStateManager;
    private final Map<DeviceStateCallback,
            android.hardware.devicestate.DeviceStateManager.DeviceStateCallback> mCallbacks =
            new ConcurrentHashMap<>();

    public interface DeviceStateCallback {
        void onDeviceStateChanged(DeviceState deviceState);

        @Deprecated
        void onStateChanged(int state);

        @Deprecated
        default void onBaseStateChanged(int state) {}

        @Deprecated
        default void onSupportedStatesChanged(int[] supportedStates) {}
    }

    public DeviceStateManager(Context context) {
        mDeviceStateManager = context.getSystemService(
                android.hardware.devicestate.DeviceStateManager.class);
    }

    public void registerCallback(Executor executor, DeviceStateCallback callback) {
        if (callback == null || mDeviceStateManager == null) {
            return;
        }
        android.hardware.devicestate.DeviceStateManager.DeviceStateCallback proxy =
                mCallbacks.computeIfAbsent(callback, cb -> state ->
                        cb.onDeviceStateChanged(new DeviceState(state)));
        mDeviceStateManager.registerCallback(executor, proxy);
    }

    public void unregisterCallback(DeviceStateCallback callback) {
        if (callback == null || mDeviceStateManager == null) {
            return;
        }
        android.hardware.devicestate.DeviceStateManager.DeviceStateCallback proxy =
                mCallbacks.remove(callback);
        if (proxy != null) {
            mDeviceStateManager.unregisterCallback(proxy);
        }
    }
}
