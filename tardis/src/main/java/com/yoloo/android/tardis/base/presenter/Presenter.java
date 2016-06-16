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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The base interface for every presenter.
 */
public interface Presenter<V extends MvpView> {

    void onCreate(@Nullable Bundle bundle);

    void onDestroy(@NonNull Bundle bundle);

    /**
     * Attach the view to this presenter.
     */
    void onViewAttached(V view);

    /**
     * Will be called when the view has been destroyed.
     */
    void onViewDetached();

    V getView();
}
