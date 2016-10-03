package com.backpackers.android.ui.timeline.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

import timber.log.Timber;

public class TimelineAdapter extends BaseAdapter<AbstractPost> {

    public void addAllToEnd(List<AbstractPost> posts) {
        // record this value before making any changes to the existing list
        final int curSize = getDataItemCount();
        Timber.d("addAllToEnd(); Old data size: %s", getDataItemCount());
        Timber.d("addAllToEnd(); New data size: %s", posts.size());

        mItems.addAll(posts);
        notifyItemInserted(curSize);

        Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    public void refreshData(List<AbstractPost> posts) {
        Timber.d("Data is coming from pullToRefresh!");

        Timber.d("refreshData(); Old data size: %s", getDataItemCount());
        Timber.d("refreshData(); New data size: %s", posts.size());

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new TimelineDiffCallback(mItems, posts));
        result.dispatchUpdatesTo(this);

        mItems.clear();
        mItems.addAll(posts);

        Timber.d("refreshData(); Renewed data size: %s", getItemCount());
    }
}