/*
 * Copyright 2016 Yasin Sinan Kayacan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yoloo.android.tardis.base.view;

import com.yoloo.android.tardis.base.presenter.Presenter;
import com.yoloo.android.tardis.common.PresenterFactory;
import com.yoloo.android.tardis.loader.PresenterLoader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

/**
 * The base class for activities that want to implement Mvp.
 *
 * @param <V> MvpView
 * @param <P> Presenter
 */
public abstract class MvpActivity<V extends MvpView, P extends Presenter<V>> extends AppCompatActivity {

    private static final int LOADER_ID = 10000;
    private P mPresenter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoader(savedInstanceState);
    }

    protected void onPresenterReady() {
        mPresenter.onViewAttached(getMvpView());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.onDestroy(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onViewDetached();
    }

    private void initLoader(@Nullable Bundle savedInstanceState) {
        getSupportLoaderManager().initLoader(LOADER_ID, savedInstanceState,
                new LoaderManager.LoaderCallbacks<P>() {
                    @Override
                    public Loader<P> onCreateLoader(int id, Bundle args) {
                        return new PresenterLoader<>(getApplicationContext(), getPresenterFactory(), args);
                    }

                    @Override
                    public void onLoadFinished(Loader<P> loader, final P presenter) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPresenter = presenter;
                                onPresenterReady();
                            }
                        });
                    }

                    @Override
                    public void onLoaderReset(Loader<P> loader) {

                    }
                });
    }

    private PresenterFactory<P> getPresenterFactory() {
        return new PresenterFactory<P>() {
            @Override
            public P create() {
                return createPresenter();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private V getMvpView() {
        return (V) this;
    }

    protected P getPresenter() {
        return mPresenter;
    }

    protected abstract P createPresenter();
}
