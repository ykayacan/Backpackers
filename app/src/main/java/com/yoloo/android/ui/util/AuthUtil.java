package com.yoloo.android.ui.util;

import android.app.Activity;
import android.content.Intent;

import com.yoloo.android.ui.signin.providers.SignInActivity;
import com.yoloo.android.util.PrefHelper;

import timber.log.Timber;

public class AuthUtil {

    public static char[] checkIsSignedIn(final Activity activity) {
        final String accessToken = PrefHelper.with(activity).getString("accessToken", null);

        Timber.d("Access token: %s", accessToken);

        if (accessToken == null) {
            activity.startActivity(new Intent(activity, SignInActivity.class));
            activity.finish();
            return null;
        }

        return accessToken.toCharArray();
    }
}
