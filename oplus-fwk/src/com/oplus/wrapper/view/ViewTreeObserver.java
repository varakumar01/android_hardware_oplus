package com.oplus.wrapper.view;

import android.graphics.Region;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ViewTreeObserver {

    private final Map<OnComputeInternalInsetsListener,
            android.view.ViewTreeObserver.OnComputeInternalInsetsListener> mListeners =
            new ConcurrentHashMap<>();
    private final android.view.ViewTreeObserver mViewTreeObserver;

    public ViewTreeObserver(android.view.ViewTreeObserver viewTreeObserver) {
        mViewTreeObserver = viewTreeObserver;
    }

    public void addOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        if (listener == null) {
            return;
        }
        android.view.ViewTreeObserver.OnComputeInternalInsetsListener impl =
                mListeners.get(listener);
        if (impl == null) {
            impl = info -> listener.onComputeInternalInsets(new InternalInsetsInfo(info));
            mListeners.put(listener, impl);
        }
        mViewTreeObserver.addOnComputeInternalInsetsListener(impl);
    }

    public void removeOnComputeInternalInsetsListener(OnComputeInternalInsetsListener listener) {
        if (listener == null) {
            return;
        }
        android.view.ViewTreeObserver.OnComputeInternalInsetsListener impl =
                mListeners.remove(listener);
        if (impl != null) {
            mViewTreeObserver.removeOnComputeInternalInsetsListener(impl);
        }
    }

    public interface OnComputeInternalInsetsListener {
        void onComputeInternalInsets(InternalInsetsInfo info);
    }

    public static final class InternalInsetsInfo {

        public static final int TOUCHABLE_INSETS_REGION =
                android.view.ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION;

        private final android.view.ViewTreeObserver.InternalInsetsInfo mInternalInsetsInfo;

        InternalInsetsInfo(android.view.ViewTreeObserver.InternalInsetsInfo info) {
            mInternalInsetsInfo = info;
        }

        public Region getTouchableRegion() {
            return mInternalInsetsInfo.touchableRegion;
        }

        public void setTouchableInsets(int val) {
            mInternalInsetsInfo.setTouchableInsets(val);
        }
    }
}
