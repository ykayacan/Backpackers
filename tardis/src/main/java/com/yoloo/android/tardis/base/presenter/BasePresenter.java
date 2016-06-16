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

package com.yoloo.android.tardis.base.presenter;

import com.yoloo.android.tardis.base.view.MvpView;

/**
 * The base implementation of {@link Presenter}
 *
 * @param <V> MvpView
 */
public abstract class BasePresenter<V extends MvpView> implements Presenter<V> {

    private V mView;

    @Override
    public void onViewAttached(V view) {
        mView = view;
    }

    @Override
    public void onViewDetached() {
        if (mView != null) {
            mView = null;
        }
    }

    /**
     * Returns current view.
     *
     * @return the view
     */
    @Override
    public V getView() {
        return mView;
    }
}
