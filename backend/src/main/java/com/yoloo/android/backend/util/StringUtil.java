package com.yoloo.android.backend.util;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

public final class StringUtil {

    public static List<String> splitValueByToken(final String args, final String rule) {
        return Arrays.asList(args.split("\\s*" + rule + "\\s*"));
    }

    public static ImmutableList<String> split(final String args,
                                              final String rule) {
        return ImmutableList.copyOf(args.split("\\s*" + rule + "\\s*"));
    }
}
