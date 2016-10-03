package com.backpackers.android.framework.base;

import com.backpackers.android.framework.PresenterFactory;
import com.backpackers.android.framework.PresenterLoader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseMvpActivity<V extends BaseMvpView, P extends MvpPresenter<V>>
        extends AppCompatActivity {

    protected final int LOADER_ID = 10000;

    private P mPresenter;

    private LoaderManager.LoaderCallbacks<P> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<P>() {
                @Override
                public Loader<P> onCreateLoader(int id, Bundle args) {
                    return new PresenterLoader<>(BaseMvpActivity.this, new PresenterFactory<P>() {
                        @Override
                        public P create() {
                            return createPresenter();
                        }
                    });
                }

                @Override
                public void onLoadFinished(Loader<P> loader, P presenter) {
                    mPresenter = presenter;
                }

                @Override
                public void onLoaderReset(Loader<P> loader) {
                    mPresenter.onDestroyed();
                    mPresenter = null;
                    onPresenterDestroyed();
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportLoaderManager().initLoader(LOADER_ID, null, mLoaderCallbacks);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onViewAttached(getPresenterView());
    }

    @Override
    protected void onStop() {
        mPresenter.onViewDetached();
        super.onStop();
    }

    protected final P getPresenter() {
        return mPresenter;
    }

    protected void onPresenterDestroyed() {
        // hook for subclasses
    }

    private V getPresenterView() {
        return (V) this;
    }

    protected abstract P createPresenter();
}
