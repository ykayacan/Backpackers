package com.yoloo.android.framework.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.yoloo.android.framework.PresenterFactory;
import com.yoloo.android.framework.PresenterLoader;

public abstract class BaseMvpFragment<V extends BaseMvpView, P extends MvpPresenter<V>> extends Fragment
        implements LoaderManager.LoaderCallbacks<P>, PresenterFactory<P> {

    protected final int LOADER_ID = 593;

    private P mPresenter;

    // boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated makes
    // onLoadFinished will be called twice during configuration change.
    private boolean mPresenterDelivered;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onViewAttached(getPresenterView());
    }

    @Override
    public void onPause() {
        mPresenter.onViewDetached();
        super.onPause();
    }

    public P getPresenter() {
        return mPresenter;
    }

    private V getPresenterView() {
        return (V) this;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(getActivity(), this);
    }

    @Override
    public void onLoadFinished(Loader<P> loader, P presenter) {
        if (!mPresenterDelivered) {
            mPresenter = presenter;
            mPresenterDelivered = true;
        }
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        mPresenter = null;
    }
}
