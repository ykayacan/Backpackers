package com.yoloo.android.framework;

import com.yoloo.android.framework.base.MvpPresenter;

public interface PresenterFactory<P extends MvpPresenter> {

    P createPresenter();
}
