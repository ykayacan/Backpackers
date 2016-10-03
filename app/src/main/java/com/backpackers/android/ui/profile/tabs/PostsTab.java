package com.backpackers.android.ui.profile.tabs;

import com.backpackers.android.R;
import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.forum.ForumFragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class PostsTab extends TabHolder {

    private String mUserId;

    public PostsTab(String userId) {
        mUserId = userId;
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return ForumFragment.newInstance(mUserId, null);
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_checkbox_pen_outline_white_24dp;
    }
}
