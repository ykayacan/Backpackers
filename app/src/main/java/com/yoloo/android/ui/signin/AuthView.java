package com.yoloo.android.ui.signin;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.framework.base.BaseMvpView;

public interface AuthView extends BaseMvpView {

    void onSaveUser(Account account);

    void onSaveToken(Token token);

    void onSuccess();

    void onError(Throwable e);
}
