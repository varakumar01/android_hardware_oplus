package com.oplus.wrapper.provider;

import android.content.ContentResolver;

public class Settings {

    public static class System {

        public static int getIntForUser(ContentResolver resolver, String name, int def,
                int userHandle) {
            return android.provider.Settings.System.getIntForUser(resolver, name, def,
                    userHandle);
        }
    }
}
