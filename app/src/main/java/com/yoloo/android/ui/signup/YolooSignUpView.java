package com.yoloo.android.ui.signup;

import com.hannesdorfmann.mosby.mvp.MvpView;
import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.backend.modal.yolooApi.model.Token;

interface YolooSignUpView extends MvpView {

    void onShowProgressDialog(boolean show);

    void onNavigateToHome();

    void onSaveUser(Account account);

    void onSaveToken(Token token);

    void onEmailExistsError();

    void onUsernameExistsError();
}
