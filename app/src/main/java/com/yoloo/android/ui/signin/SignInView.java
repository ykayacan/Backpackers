package com.yoloo.android.ui.signin;

import com.hannesdorfmann.mosby.mvp.MvpView;
import com.yoloo.android.backend.modal.yolooApi.model.Account;

public interface SignInView extends MvpView {

    void onNavigateToHome();

    void onSaveUser(Account account);

    void onEmailExistsError();
}
