package com.backpackers.android.ui.search.tabs;

import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.search.SearchHashTagFragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class SearchHashTagTab extends TabHolder {

    private final String mName;

    public SearchHashTagTab(String name) {
        mName = name;
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return SearchHashTagFragment.newInstance();
    }

    @Nullable
    @Override
    public String getText() {
        return mName;
    }
}
