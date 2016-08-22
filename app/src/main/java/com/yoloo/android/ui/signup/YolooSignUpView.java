package com.yoloo.android.ui.signup;

import com.yoloo.android.ui.signin.AuthView;

interface YolooSignUpView extends AuthView {

    void onShowProgress(boolean show);

    void onEmailExistsError();

    void onUsernameExistsError();
}
