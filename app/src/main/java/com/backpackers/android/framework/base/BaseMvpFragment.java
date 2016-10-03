package com.backpackers.android.framework.base;

import com.backpackers.android.framework.PresenterFactory;
import com.backpackers.android.framework.PresenterLoader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class BaseMvpFragment<V extends BaseMvpView, P extends MvpPresenter<V>>
        extends Fragment {

    protected final int LOADER_ID = 10001;

    private P mPresenter;

    // boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated makes
    // onLoadFinished will be called twice during configuration change.
    private boolean mPresenterDelivered;

    private LoaderManager.LoaderCallbacks<P> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<P>() {
                @Override
                public final Loader<P> onCreateLoader(int id, Bundle args) {
                    return new PresenterLoader<>(getContext(), new PresenterFactory<P>() {
                        @Override
                        public P create() {
                            return createPresenter();
                        }
                    });
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
                    mPresenter.onDestroyed();
                    mPresenter = null;
                    onPresenterDestroyed();
                }
            };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, mLoaderCallbacks);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onViewAttached(getPresenterView());
    }

    @Override
    public void onStop() {
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
