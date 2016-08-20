package com.yoloo.android.framework;

import com.yoloo.android.framework.base.BasePresenter;

public interface PresenterStorage {
    BasePresenter create(String tag);
}
