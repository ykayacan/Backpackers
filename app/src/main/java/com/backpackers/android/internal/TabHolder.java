package com.backpackers.android.internal;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public abstract class TabHolder {

    @NonNull
    public abstract Fragment getFragment();

    @Nullable
    public String getText() {
        return null;
    }

    @DrawableRes
    public int getDrawable() {
        return -1;
    }

    @ColorRes
    public int getBackgroundColor() {
        return -1;
    }

    @ColorRes
    public int getStatusBarColor() {
        return -1;
    }
}
