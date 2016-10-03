package com.backpackers.android.ui.forum.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ForumAdapter extends BaseAdapter<ForumPost> {

    private ForumDiffCallback mForumDiffCallback = new ForumDiffCallback();

    public void addToTop(ForumPost post) {
        mItems.add(0, post);
        notifyItemInserted(0);
    }

    public void addAllToEnd(List<ForumPost> posts) {
        // record this value before making any changes to the existing list
        final int curSize = getDataItemCount();

        //Timber.d("addAllToEnd(); Old data size: %s", getDataItemCount());
        //Timber.d("addAllToEnd(); New data size: %s", posts.size());

        mItems.addAll(posts);
        notifyItemRangeInserted(curSize, getDataItemCount());

        //Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    public void refreshData(final List<ForumPost> posts, final RecyclerView recyclerView) {
        Timber.d("Data is coming from pullToRefresh!");

        mForumDiffCallback.setOldList(mItems);
        mForumDiffCallback.setNewList(posts);

        calculateDiff(mForumDiffCallback)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        recyclerView.smoothScrollToPosition(0);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(posts);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void update(ForumPost post) {
        for (ForumPost p : mItems) {
            if (p.getId().equals(post.getId())) {

                p.setUps(post.getUps());
                p.setDowns(post.getDowns());

                int pos = mItems.indexOf(p);
                notifyItemChanged(pos);
                break;
            }
        }
    }
}
