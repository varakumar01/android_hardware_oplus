package android.app;

import android.content.ComponentName;
import android.os.RemoteException;

import com.oplus.app.OplusTaskInfoChangeListener;

import java.util.ArrayList;
import java.util.List;

public class OplusActivityTaskManager {

    private static OplusActivityTaskManager sInstance;

    public static OplusActivityTaskManager getInstance() {
        if (sInstance == null) {
            sInstance = new OplusActivityTaskManager();
        }
        return sInstance;
    }

    public ComponentName getTopActivityComponentName() throws RemoteException {
        return null;
    }

    public List<ActivityManager.RunningTaskInfo> getVisibleTasks(int displayId)
            throws RemoteException {
        return new ArrayList<>();
    }

    public boolean registerTaskInfoChangeListener(OplusTaskInfoChangeListener listener,
            int displayId, int flags) throws RemoteException {
        return false;
    }

    public boolean unregisterTaskInfoChangeListener(OplusTaskInfoChangeListener listener)
            throws RemoteException {
        return false;
    }
}
