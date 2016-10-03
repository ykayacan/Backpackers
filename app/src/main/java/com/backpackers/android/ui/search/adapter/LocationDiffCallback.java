package com.backpackers.android.ui.search.adapter;

import com.google.android.gms.location.places.PlaceLikelihood;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class LocationDiffCallback extends DiffUtil.Callback {

    private List<PlaceLikelihood> mOldList;
    private List<PlaceLikelihood> mNewList;

    public void setOldList(List<PlaceLikelihood> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<PlaceLikelihood> newList) {
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
        return mOldList.get(oldItemPosition).getPlace().getId()
                .equals(mNewList.get(newItemPosition).getPlace().getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return  mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }
}
