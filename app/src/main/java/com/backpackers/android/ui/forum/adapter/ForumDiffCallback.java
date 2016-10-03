package com.backpackers.android.ui.forum.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class ForumDiffCallback extends DiffUtil.Callback {

    private List<ForumPost> mOldList;
    private List<ForumPost> mNewList;

    public void setOldList(List<ForumPost> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<ForumPost> newList) {
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
