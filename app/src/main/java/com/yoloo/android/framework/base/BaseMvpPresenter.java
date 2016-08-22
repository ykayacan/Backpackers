package com.yoloo.android.framework.base;

public abstract class BaseMvpPresenter<V extends BaseMvpView> implements MvpPresenter<V> {

    private V mView;

    protected V getView() {
        return mView;
    }

    protected boolean isViewAttached() {
        return mView != null;
    }

    @Override
    public void onViewAttached(V view) {
        mView = view;
    }

    @Override
    public void onViewDetached() {
        mView = null;
    }

    @Override
    public void onDestroyed() {

    }
}
