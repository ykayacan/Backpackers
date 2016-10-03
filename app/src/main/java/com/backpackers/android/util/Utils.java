package com.backpackers.android.util;

import android.os.Build;

public class Utils {

    public static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
