package com.backpackers.android.util;

import com.backpackers.android.data.model.YolooError;

public class ErrorUtil {

    public static YolooError parse(final Throwable e) {
        final String message = e.getMessage();
        final String json = message.substring(message.indexOf("{"));
        return ParseUtil.getGSON().fromJson(json, YolooError.class);
    }
}
