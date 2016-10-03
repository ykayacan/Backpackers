package com.backpackers.android.ui.signin;

import android.support.annotation.IntDef;

import static com.backpackers.android.ui.signin.AuthProvider.PROVIDER_FACEBOOK;
import static com.backpackers.android.ui.signin.AuthProvider.PROVIDER_GOOGLE;
import static com.backpackers.android.ui.signin.AuthProvider.PROVIDER_YOLOO;

@IntDef({
        PROVIDER_GOOGLE,
        PROVIDER_FACEBOOK,
        PROVIDER_YOLOO
})
public @interface AuthProvider {

    int PROVIDER_GOOGLE = 0;
    int PROVIDER_FACEBOOK = 1;
    int PROVIDER_YOLOO = 2;
}
