package com.oplus.wrapper.view;

public class View {

    private final android.view.View mView;

    public View(android.view.View view) {
        mView = view;
    }

    public ViewRootImpl getViewRootImpl() {
        android.view.ViewRootImpl viewRootImpl = mView.getViewRootImpl();
        if (viewRootImpl == null) {
            return null;
        }
        return new ViewRootImpl(viewRootImpl);
    }
}
