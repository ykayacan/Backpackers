package com.backpackers.android.ui.comment.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Comment;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class CommentDiffCallback extends DiffUtil.Callback {

    private List<Comment> mOldList;
    private List<Comment> mNewList;

    public void setOldList(List<Comment> oldList) {
        mOldList = oldList;
    }

    public void setNewList(List<Comment> newList) {
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
