package com.oplus.wrapper.os;

public class UserHandle {

    public static final android.os.UserHandle OWNER = android.os.UserHandle.SYSTEM;
    public static final int USER_SYSTEM = android.os.UserHandle.USER_SYSTEM;

    public static int myUserId() {
        return android.os.UserHandle.myUserId();
    }
}
