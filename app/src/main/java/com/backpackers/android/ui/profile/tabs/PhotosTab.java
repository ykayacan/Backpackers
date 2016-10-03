package com.backpackers.android.ui.profile.tabs;

import com.backpackers.android.R;
import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.profile_photos.ProfilePhotoFragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class PhotosTab extends TabHolder {

    private final String mUserId;

    public PhotosTab(String userId) {
        mUserId = userId;
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return ProfilePhotoFragment.newInstance(mUserId);
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_camera_alt_black_24dp;
    }
}
