package com.oplus.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IOplusProtectConnection extends IInterface {

    void onSuccess() throws RemoteException;

    void onError(int errorCode) throws RemoteException;

    void onTimeout() throws RemoteException;

    public static abstract class Stub extends Binder implements IOplusProtectConnection {
        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
