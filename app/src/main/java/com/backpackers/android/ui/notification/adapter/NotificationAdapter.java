package com.backpackers.android.ui.notification.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Notification;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationAdapter extends BaseAdapter<Notification> {

    private NotificationDiffCallback mNotificationDiffCallback = new NotificationDiffCallback();

    public void addToTop(Notification notification) {
        mItems.add(0, notification);
        notifyItemInserted(0);
    }

    public void addAllToEnd(List<Notification> posts) {
        // record this value before making any changes to the existing list
        final int curSize = getDataItemCount();

        //Timber.d("addAllToEnd(); Old data size: %s", getDataItemCount());
        //Timber.d("addAllToEnd(); New data size: %s", posts.size());

        mItems.addAll(posts);
        notifyItemRangeInserted(curSize, getDataItemCount());

        //Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    public void refreshData(final List<Notification> notifications) {
        Timber.d("Data is coming from pullToRefresh!");

        mNotificationDiffCallback.setOldList(mItems);
        mNotificationDiffCallback.setNewList(notifications);

        calculateDiff(mNotificationDiffCallback)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(notifications);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }
}
