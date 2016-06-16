package com.yoloo.android.ui.login;

import com.yoloo.android.tardis.base.presenter.Presenter;

public interface LoginPresenter extends Presenter<LoginView> {

    void login(boolean refresh);
}
