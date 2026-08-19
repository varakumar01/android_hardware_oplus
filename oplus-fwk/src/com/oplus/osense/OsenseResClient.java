/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.osense;

import android.os.Bundle;
import android.view.MotionEvent;

import java.util.concurrent.atomic.AtomicLong;

public class OsenseResClient {
    // Closed-source vendor callers (e.g. the Goodix fingerprint HAL, tag
    // "GF_HAL"/"android.hardware.biometrics.fingerprint@2.1-service" in
    // logcat) reflect into this class expecting a real scene-action
    // implementation. This class has no visible caller anywhere in the
    // open-source tree, so the request/response contract below is inferred
    // purely from observed behavior, not from a known vendor source:
    // osenseSetSceneAction returning 0 (the previous, permanent behavior of
    // this stub) is logged by at least one caller as "osense setAction
    // fail" (54x per boot session in testing), which is the conventional
    // "invalid handle" sentinel -- 0/null generally means "request
    // rejected" in this style of handle-based API.
    //
    // This does NOT reimplement OPLUS's actual scene-action behavior (no
    // CPU/scheduler boost is applied here) -- we don't have visibility into
    // what the real handle should encode or what side effects the vendor
    // implementation has beyond "return a handle the caller treats as
    // valid". This only stops every caller from believing its request was
    // rejected. If you have the real vendor implementation's expected
    // semantics (field layout of the request object, what the handle
    // should represent, what boost mechanism it should trigger), replace
    // this with the real logic -- see .claude/PLAN.md's fingerprint
    // slow-registration entry for what's still open.
    private static final AtomicLong sNextHandle = new AtomicLong(1);

    public static OsenseResClient get(Class clazz) {
        return new OsenseResClient();
    }

    public void requestSysResource(int eventId, Bundle extra) {}

    public void releaseSysResource(int eventId) {}

    public long osenseSetSceneAction(Object request) {
        return sNextHandle.getAndIncrement();
    }

    public void osenseClrSceneAction(long handle) {}

    public void osenseSetNotification(Object request) {}

    public void osenseSetCtrlData(Object request) {}

    public void osenseClrCtrlData() {}

    public void reportKeyThread(String threadName, int tid, int supportedEventId, Bundle extra) {}

    public void removeKeyThread(int tid, Bundle extra) {}

    public void removeKeyThread(String threadName, Bundle extra) {}

    public void setHookKeyThread(String packageName, String threadName, int op, int pid, int[] tids) {}

    public void reportEvent(int eventId, Bundle extra) {}

    public void registerScene(int sceneId, Object listener) {}

    public void unregisterScene(int sceneId, Object listener) {}

    public int osenseGetModeStatus(int mode) {
        return 0;
    }

    public long[][][] osenseGetPerfLimit() {
        return null;
    }

    public void osenseSendFling(MotionEvent ev, int duration) {}
}
