package com.yoloo.android.ui.home.tabs;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;

public interface Tab {

    Fragment getFragment();

    @ColorInt
    int getColorInt();

    @DrawableRes
    int getDrawableRes();
}
