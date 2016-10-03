package com.backpackers.android.ui.comment.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CommentAdapter extends BaseAdapter<Comment> {

    private final CommentDiffCallback mCommentDiffCallback = new CommentDiffCallback();

    private boolean mIsComment = false;

    public void addAllToEnd(List<Comment> posts) {
        // When we add comment from phone, also data comes from server via load more.
        // Let's prevent this behaviour.
        if (mIsComment) {
            return;
        }

        // Record this value before making any changes to the existing list.
        final int curSize = getDataItemCount();

        mItems.addAll(posts);
        notifyItemRangeInserted(curSize, getDataItemCount());
    }

    public void addToEnd(Comment comment) {
        mIsComment = true;
        mItems.add(comment);
        notifyItemInserted(getDataItemCount());
    }

    public void refreshData(final List<Comment> comments) {
        Timber.d("Data is coming from pullToRefresh!");

        mCommentDiffCallback.setOldList(mItems);
        mCommentDiffCallback.setNewList(comments);

        calculateDiff(mCommentDiffCallback)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(comments);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }
}
