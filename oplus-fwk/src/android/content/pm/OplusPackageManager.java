package android.content.pm;

import android.content.Context;

public class OplusPackageManager {

    private static OplusPackageManager sInstance;

    public static OplusPackageManager getOplusPackageManager(Context context) {
        if (sInstance == null) {
            sInstance = new OplusPackageManager();
        }
        return sInstance;
    }
}
