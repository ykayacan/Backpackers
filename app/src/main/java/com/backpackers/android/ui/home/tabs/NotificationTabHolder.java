package com.backpackers.android.ui.home.tabs;

import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.R;
import com.backpackers.android.ui.notification.NotificationFragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class NotificationTabHolder extends TabHolder {

    public static NotificationTabHolder newInstance() {
        return new NotificationTabHolder();
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return NotificationFragment.newInstance();
    }

    @Override
    public int getDrawable() {
        return R.drawable.ic_notifications_white_24dp;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.colorPrimaryGreen;
    }

    @Override
    public int getStatusBarColor() {
        return R.color.colorPrimaryDarkGreen;
    }
}
