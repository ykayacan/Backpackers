package com.backpackers.android.ui.home.tabs;

import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.forum.ForumFragment;
import com.backpackers.android.R;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class ForumTabHolder extends TabHolder {

    public static ForumTabHolder newInstance() {
        return new ForumTabHolder();
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return ForumFragment.newInstance(null, null);
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_forum_white_24dp;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.colorPrimaryBlue;
    }

    @Override
    public int getStatusBarColor() {
        return R.color.colorPrimaryDarkBlue;
    }
}
