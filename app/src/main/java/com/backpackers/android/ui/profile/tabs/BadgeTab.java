package com.backpackers.android.ui.profile.tabs;

import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.profile_badge.ProfileBadgeFragment;
import com.backpackers.android.R;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class BadgeTab extends TabHolder {
    private final String mUserId;

    public BadgeTab(String userId) {
        mUserId = userId;
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return ProfileBadgeFragment.newInstance();
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_medal_white_24dp;
    }
}
