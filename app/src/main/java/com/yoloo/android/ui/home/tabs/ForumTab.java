package com.yoloo.android.ui.home.tabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.yoloo.android.R;
import com.yoloo.android.ui.post.PostFragment;

public class ForumTab implements Tab {

    private final Context mContext;
    private final char[] mAccessToken;

    private ForumTab(Context context, char[] accessToken) {
        mContext = context;
        mAccessToken = accessToken;
    }

    public static ForumTab newInstance(Context context, char[] accessToken) {
        return new ForumTab(context, accessToken);
    }

    @Override
    public Fragment getFragment() {
        return PostFragment.newInstance(mAccessToken);
    }

    @Override
    public int getColorInt() {
        return ContextCompat.getColor(mContext, R.color.tab_forum);
    }

    @Override
    public int getDrawableRes() {
        return R.drawable.selector_tab_forum;
    }
}
