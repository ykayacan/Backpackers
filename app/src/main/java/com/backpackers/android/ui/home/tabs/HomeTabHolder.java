package com.backpackers.android.ui.home.tabs;

import com.backpackers.android.R;
import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.timeline.TimelineFragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class HomeTabHolder extends TabHolder {

    public static HomeTabHolder newInstance() {
        return new HomeTabHolder();
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return TimelineFragment.newInstance();
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_home_white_24dp;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.colorPrimary;
    }
}
