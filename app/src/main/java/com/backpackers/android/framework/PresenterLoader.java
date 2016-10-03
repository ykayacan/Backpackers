package com.backpackers.android.framework;

import com.backpackers.android.framework.base.MvpPresenter;

import android.content.Context;
import android.support.v4.content.Loader;

public class PresenterLoader<P extends MvpPresenter> extends Loader<P> {

    private final PresenterFactory<P> mFactory;
    private P mPresenter;

    public PresenterLoader(Context context, PresenterFactory<P> mFactory) {
        super(context);
        this.mFactory = mFactory;
    }

    @Override
    protected void onStartLoading() {
        if (mPresenter != null) {
            deliverResult(mPresenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        mPresenter = mFactory.create();
        deliverResult(mPresenter);
    }

    @Override
    protected void onReset() {
        if (mPresenter != null) {
            mPresenter.onDestroyed();
            mPresenter = null;
        }
    }
}
