package com.backpackers.android.ui.follow.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FollowAdapter extends BaseAdapter<Account> {

    private FollowDiffCallback mFollowDiffCallback = new FollowDiffCallback();

    public void addToTop(Account account) {
        mItems.add(0, account);
        notifyItemInserted(0);
    }

    public void addAllToEnd(List<Account> accounts) {
        // record this value before making any changes to the existing list
        final int curSize = getDataItemCount();

        //Timber.d("addAllToEnd(); Old data size: %s", getDataItemCount());
        //Timber.d("addAllToEnd(); New data size: %s", posts.size());

        mItems.addAll(accounts);
        notifyItemRangeInserted(curSize, getDataItemCount());

        //Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    public void refreshData(final List<Account> accounts) {
        Timber.d("Data is coming from pullToRefresh!");

        mFollowDiffCallback.setOldList(mItems);
        mFollowDiffCallback.setNewList(accounts);

        calculateDiff(mFollowDiffCallback)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(accounts);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }
}
