package com.backpackers.android.ui.profile_photos.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Media;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class ProfilePhotoDiffCallback extends DiffUtil.Callback {

    private List<Media> mOldList;
    private List<Media> mNewList;

    public void setOldList(List<Media> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<Media> newList) {
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
