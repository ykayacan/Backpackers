package com.yoloo.android.framework;

import com.yoloo.android.framework.base.BasePresenter;

import android.content.Context;
import android.support.v4.content.Loader;

public class PresenterLoader<P extends BasePresenter> extends Loader<P> {
    private P mPresenter;

    private String mTagPresenter;

    public PresenterLoader(Context context, String tagPresenter) {
        super(context);

        mTagPresenter = tagPresenter;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (mPresenter != null) {
            deliverResult(mPresenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        mPresenter = (P) PresenterFactory.getInstance().create(mTagPresenter);
        deliverResult(mPresenter);
    }

    @Override
    protected void onReset() {
        mPresenter.onDestroy();
        mPresenter = null;
    }
}
