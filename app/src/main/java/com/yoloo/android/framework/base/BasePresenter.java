package com.yoloo.android.framework.base;

public abstract class BasePresenter<V> {
    protected V mView;

    protected boolean isViewBinded() {
        return mView != null;
    }

    public void bindView(V view) {
        mView = view;
    }

    public void unbindView() {
        mView = null;
    }

    public void onDestroy() {
    }
}
