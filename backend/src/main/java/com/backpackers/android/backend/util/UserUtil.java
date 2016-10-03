package com.backpackers.android.backend.util;

import com.google.api.client.util.Strings;

import com.backpackers.android.backend.Config;

public class UserUtil {

    public static String setUsername(String username) {
        return username.toLowerCase().trim().replaceAll("\\s+", "");
    }

    public static String setProfileImage(String url) {
        return Strings.isNullOrEmpty(url) ? Config.DUMMY_PROFILE_IMAGE : url;
    }
}
