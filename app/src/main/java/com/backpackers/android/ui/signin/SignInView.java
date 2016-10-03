package com.backpackers.android.ui.signin;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.framework.base.BaseMvpView;

public interface SignInView extends BaseMvpView {

    void onSaveUser(Account account);

    void onSaveToken(Token token);

    void onSuccess();

    void onShowProgress(boolean show, boolean isSignIn);

    void onEmailExistsError(@AuthProvider int provider);

    void onUsernameExistsError();

    void onNetworkError();

    void onUnauthorized();

    void onInvalidEmail();

    void onInvalidPassword();
}
