package com.yoloo.android.backend.util;

import java.util.Arrays;
import java.util.List;

public final class StringUtil {

    @Deprecated
    public static List<String> splitValueByComma(final String value) {
        return Arrays.asList(value.split("\\s*,\\s*"));
    }

    public static List<String> splitValueByToken(final String args, final String rule) {
        return Arrays.asList(args.split("\\s*" + rule + "\\s*"));
    }
}
