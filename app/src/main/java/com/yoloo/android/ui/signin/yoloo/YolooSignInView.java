package com.yoloo.android.ui.signin.yoloo;

import com.hannesdorfmann.mosby.mvp.MvpView;
import com.yoloo.android.backend.modal.yolooApi.model.Token;

public interface YolooSignInView extends MvpView {

    void onShowProgressDialog(boolean show);

    void onNavigateToHome();

    void onSaveToken(Token token);

    void onError();
}
