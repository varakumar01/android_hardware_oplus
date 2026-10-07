package com.oplus.wrapper.hardware.devicestate;

public final class DeviceState {

    private final android.hardware.devicestate.DeviceState mDeviceState;

    DeviceState(android.hardware.devicestate.DeviceState deviceState) {
        mDeviceState = deviceState;
    }

    public int getIdentifier() {
        return mDeviceState.getIdentifier();
    }
}
