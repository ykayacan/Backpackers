package com.yoloo.android.ui.login;

import com.hannesdorfmann.mosby.mvp.MvpView;

import android.content.Intent;

interface LoginView extends MvpView {

    void onShowProgressDialog(boolean show);

    void navigateToHome();

    void onStartActivityForResult(Intent intent);
}
