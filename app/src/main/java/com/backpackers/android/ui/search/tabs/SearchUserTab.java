package com.backpackers.android.ui.search.tabs;

import com.backpackers.android.internal.TabHolder;
import com.backpackers.android.ui.search.SearchUserFragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class SearchUserTab extends TabHolder {

    private final String mText;

    public SearchUserTab(String text) {
        mText = text;
    }

    @NonNull
    @Override
    public Fragment getFragment() {
        return SearchUserFragment.newInstance();
    }

    @Nullable
    @Override
    public String getText() {
        return mText;
    }
}
