package com.yoloo.android.framework;

import android.content.Context;
import android.support.v4.content.Loader;

import com.yoloo.android.framework.base.MvpPresenter;

import timber.log.Timber;

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
            Timber.d("onStartLoading(): delivered.");
            deliverResult(mPresenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        mPresenter = mFactory.createPresenter();
        Timber.d("onForceLoad() - New presenter is created: %s", mPresenter.getClass().getName());
        deliverResult(mPresenter);
    }

    @Override
    protected void onReset() {
        if (mPresenter != null) {
            Timber.d("onReset(): presenter destroyed.");
            mPresenter.onDestroyed();
            mPresenter = null;
        }
    }
}
