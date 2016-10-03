package com.backpackers.android.util;

import com.backpackers.android.Constants;

import android.content.Context;
import android.util.Base64;

public class AuthUtils {

    public static boolean shouldNavigateToSignIn(Context context) {
        return !PrefUtils.with(context).contains(Constants.PREF_KEY_ACCESS_TOKEN);
    }

    public static boolean isProviderGoogle(Context context) {
        return PrefUtils.with(context).getString(Constants.PREF_KEY_PROVIDER, "")
                .equals(Constants.PROVIDER_GOOGLE);
    }

    public static boolean isProviderFacebook(Context context) {
        return PrefUtils.with(context).getString(Constants.PREF_KEY_PROVIDER, "")
                .equals(Constants.PROVIDER_FACEBOOK);
    }

    public static boolean isProviderYoloo(Context context) {
        return PrefUtils.with(context).getString(Constants.PREF_KEY_PROVIDER, "")
                .equals(Constants.PROVIDER_YOLOO);
    }

    public static String getEncodedValue(String username, String email, String password) {
        final String credentials = username + ":" + password + ":" + email;
        return Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
    }
}
