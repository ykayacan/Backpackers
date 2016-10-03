package com.backpackers.android.framework.base;

import rx.Subscription;

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

    public void unSubscribe(Subscription... subscriptions) {
        for (Subscription s : subscriptions) {
            if (s != null && !s.isUnsubscribed()) {
                s.unsubscribe();
            }
        }
    }
}
