package com.backpackers.android.ui.notification.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Notification;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class NotificationDiffCallback extends DiffUtil.Callback {

    private List<Notification> mOldList;
    private List<Notification> mNewList;

    public void setOldList(List<Notification> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<Notification> newList) {
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
