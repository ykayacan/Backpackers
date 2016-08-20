package com.yoloo.android.util;

import com.google.gson.Gson;

import com.yoloo.android.data.model.YolooError;

public class ErrorUtil {

    private static final Gson GSON = new Gson();

    public static YolooError parse(final Throwable e) {
        final String message = e.getMessage();
        final String json = message.substring(message.indexOf("{"));
        return GSON.fromJson(json, YolooError.class);
    }
}
