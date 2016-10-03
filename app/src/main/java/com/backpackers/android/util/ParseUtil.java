package com.backpackers.android.util;

import com.google.gson.Gson;

public class ParseUtil {

    private static Gson GSON = null;

    public static Gson getGSON() {
        if (GSON == null) {
            GSON = new Gson();
        }
        return GSON;
    }
}
