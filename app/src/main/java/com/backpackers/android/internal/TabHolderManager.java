package com.backpackers.android.internal;

import android.support.annotation.IntRange;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class TabHolderManager {

    /**
     * Container to hold Tabs.
     */
    private List<TabHolder> mTabs = new ArrayList<>(3);

    /**
     * Add tab holder tab manager.
     *
     * @param tabHolder the tab holder
     * @return the tab manager
     */
    public TabHolderManager addTabHolder(TabHolder tabHolder) {
        mTabs.add(tabHolder);
        return this;
    }

    /**
     * Remove tab holder at.
     *
     * @param index the index
     */
    public void removeTabHolderAt(@IntRange(from = 0) int index) {
        mTabs.remove(index);
    }

    /**
     * Gets tab holders.
     *
     * @return the tab holders
     */
    public List<TabHolder> getTabHolders() {
        return mTabs;
    }

    /**
     * Get tab holder test.
     *
     * @param index the index
     * @return the tab holder test
     */
    public TabHolder get(@IntRange(from = 0) int index) {
        return mTabs.get(index);
    }

    /**
     * Sets tabs with only icon.
     *
     * @param tabLayout the tab layout
     */
    public void setupTabsWithOnlyIcon(TabLayout tabLayout) {
        if (mTabs.isEmpty()) {
            throw new RuntimeException("Tabs are empty. Please add at least one tab.");
        }

        for (TabHolder holder : mTabs) {
            if (holder.getDrawable() == -1) {
                throw new IllegalArgumentException("Tab icon is not specified.");
            }
            tabLayout.addTab(tabLayout.newTab().setIcon(holder.getDrawable()));
        }
    }

    /**
     * Sets tabs with only text.
     *
     * @param tabLayout the tab layout
     */
    public void setupTabsWithOnlyText(TabLayout tabLayout) {
        if (mTabs.isEmpty()) {
            throw new RuntimeException("Tabs are empty. Please add at least one tab.");
        }

        for (TabHolder holder : mTabs) {
            if (TextUtils.isEmpty(holder.getText())) {
                throw new IllegalArgumentException("Tab text is null.");
            }
            tabLayout.addTab(tabLayout.newTab().setText(holder.getText()));
        }
    }
}
