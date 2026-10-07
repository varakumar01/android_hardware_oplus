package android.app;

import android.content.Context;

import com.oplus.app.IOplusProtectConnection;

import java.util.ArrayList;

public class OplusWhiteListManager {

    public OplusWhiteListManager(Context context) {}

    public ArrayList<String> getStageProtectListFromPkg(String calledPkg, int type) {
        return new ArrayList<>();
    }

    public void addStageProtectInfo(String pkg, long timeout) {}

    public void addStageProtectInfo(String pkg, String reason, long timeout,
            IOplusProtectConnection connection) {}

    public void removeStageProtectInfo(String pkg) {}
}
