package com.backpackers.android.ui.follow.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Account;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class FollowDiffCallback extends DiffUtil.Callback {

    private List<Account> mOldList;
    private List<Account> mNewList;

    public void setOldList(List<Account> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<Account> newList) {
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
