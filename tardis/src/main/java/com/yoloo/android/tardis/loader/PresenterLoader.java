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

package com.yoloo.android.tardis.loader;

import com.yoloo.android.tardis.base.presenter.Presenter;
import com.yoloo.android.tardis.base.view.MvpView;
import com.yoloo.android.tardis.common.PresenterFactory;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;

/**
 * The core class which handles of all {@link Presenter} lifecycle.
 *
 * @param <V> MvpView
 * @param <P> Presenter
 */
public class PresenterLoader<V extends MvpView, P extends Presenter<V>> extends Loader<P> {

    private PresenterFactory<P> mFactory;
    private Bundle mSavedStateBundle;
    private P mPresenter;

    /**
     * Instantiates a new Presenter loader.
     *
     * @param context  the context
     * @param factory the callback
     */
    public PresenterLoader(Context context, PresenterFactory<P> factory, Bundle savedStateBundle) {
        super(context);
        mFactory = factory;
        mSavedStateBundle = savedStateBundle;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        // We already have a presenter, so pass it.
        if (mPresenter != null) {
            deliverResult(mPresenter);
            return;
        }

        // Otherwise, force a load.
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();

        // Create a presenter
        mPresenter = mFactory.create();
        mPresenter.onCreate(mSavedStateBundle);

        clearDataAfterCreation();

        // Deliver result
        deliverResult(mPresenter);
    }

    private void clearDataAfterCreation() {
        mFactory = null;
        mSavedStateBundle = null;
    }
}
