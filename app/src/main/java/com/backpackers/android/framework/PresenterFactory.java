package com.backpackers.android.framework;

import com.backpackers.android.framework.base.MvpPresenter;

public interface PresenterFactory<P extends MvpPresenter> {

    P create();
}
