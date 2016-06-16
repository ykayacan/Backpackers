package com.yoloo.android.ui.base;

import com.yoloo.android.tardis.base.presenter.Presenter;
import com.yoloo.android.tardis.base.view.MvpActivity;
import com.yoloo.android.tardis.base.view.MvpView;

import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class BaseActivity<V extends MvpView, P extends Presenter<V>> extends MvpActivity<V, P> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
