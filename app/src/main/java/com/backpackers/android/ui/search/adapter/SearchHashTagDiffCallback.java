package com.backpackers.android.ui.search.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.HashTag;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class SearchHashTagDiffCallback extends DiffUtil.Callback {

    private List<HashTag> mOldList;
    private List<HashTag> mNewList;

    public void setOldList(List<HashTag> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<HashTag> newList) {
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
        return mOldList.get(oldItemPosition).getHashTag().equals(mNewList.get(newItemPosition).getHashTag());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }
}
