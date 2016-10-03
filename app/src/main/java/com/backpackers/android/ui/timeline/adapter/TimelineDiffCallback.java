package com.backpackers.android.ui.timeline.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class TimelineDiffCallback extends DiffUtil.Callback {

    private List<AbstractPost> mOldList;
    private List<AbstractPost> mNewList;

    public TimelineDiffCallback(List<AbstractPost> oldList, List<AbstractPost> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getId().equals(mNewList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }
}
