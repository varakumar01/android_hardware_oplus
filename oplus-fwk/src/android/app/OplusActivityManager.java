package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import com.oplus.app.OplusAppInfo;
import com.oplus.osense.complexscene.OplusComplexSceneObserver;

import java.util.ArrayList;
import java.util.List;

public class OplusActivityManager {

    private static OplusActivityManager sOplusActivityManager = null;
    private static ArrayList<OplusAppInfo> sTopAppInfos = new ArrayList<OplusAppInfo>();

    public static OplusActivityManager getInstance() {
        if (sOplusActivityManager == null) {
            sOplusActivityManager = new OplusActivityManager();
        }
        return sOplusActivityManager;
    }

    public List<OplusAppInfo> getAllTopAppInfos() throws RemoteException {
        return (ArrayList<OplusAppInfo>) sTopAppInfos.clone();
    }

    public void startActivity(Intent intent) {}

    public boolean requestDeviceFolded(int state, boolean folded) {
        return false;
    }

    public ComponentName getTopActivityComponentName() {
        return null;
    }

    public boolean registerComplexSceneObserver(Bundle options,
            OplusComplexSceneObserver observer) {
        return false;
    }

    public boolean unregisterComplexSceneObserver(Bundle options,
            OplusComplexSceneObserver observer) {
        return false;
    }
}
