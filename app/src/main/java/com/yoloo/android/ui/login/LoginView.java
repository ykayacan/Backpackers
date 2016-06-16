package com.yoloo.android.ui.login;

import com.yoloo.android.tardis.base.view.MvpView;

public interface LoginView extends MvpView {

    void showLoading();

    void showContent();

    void setData(String text);
}
