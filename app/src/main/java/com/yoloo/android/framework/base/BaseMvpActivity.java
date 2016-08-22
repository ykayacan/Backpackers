package com.yoloo.android.framework.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.yoloo.android.framework.PresenterFactory;
import com.yoloo.android.framework.PresenterLoader;

public abstract class BaseMvpActivity<V extends BaseMvpView, P extends MvpPresenter<V>> extends AppCompatActivity
        implements BaseMvpView, LoaderManager.LoaderCallbacks<P>, PresenterFactory<P> {

    protected final int LOADER_ID = 342;

    private P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onViewAttached((V) this);
    }

    @Override
    protected void onStop() {
        mPresenter.onViewDetached();
        super.onStop();
    }

    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(this, this);
    }

    @Override
    public void onLoadFinished(Loader<P> loader, P presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        mPresenter.onDestroyed();
        mPresenter = null;
    }
}
