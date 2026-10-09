package com.oplus.wrapper.bluetooth;

public class BluetoothDevice {

    private final android.bluetooth.BluetoothDevice mBluetoothDevice;

    public BluetoothDevice(android.bluetooth.BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
    }

    public boolean isConnected() {
        try {
            return (boolean) android.bluetooth.BluetoothDevice.class.getMethod("isConnected")
                    .invoke(mBluetoothDevice);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
