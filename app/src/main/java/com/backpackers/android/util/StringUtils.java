package com.backpackers.android.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StringUtils {

    private static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();

    static {
        SUFFIXES.put(1_000L, "k");
        SUFFIXES.put(1_000_000L, "M");
        SUFFIXES.put(1_000_000_000L, "G");
    }

    public static String format(Long value) {
        if (value < 0) return "0";
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        final Map.Entry<Long, String> e = SUFFIXES.floorEntry(value);
        final Long divideBy = e.getKey();
        final String suffix = e.getValue();

        final long truncated = value / (divideBy / 10); //the number part of the output times 10
        final boolean hasDecimal = truncated < 1000 && (truncated / 100d) != (truncated / 100);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
