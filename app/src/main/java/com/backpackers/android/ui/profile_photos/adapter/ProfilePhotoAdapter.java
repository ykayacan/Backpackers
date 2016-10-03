package com.backpackers.android.ui.profile_photos.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Media;
import com.backpackers.android.ui.recyclerview.BaseAdapter;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ProfilePhotoAdapter extends BaseAdapter<Media> {

    private ProfilePhotoDiffCallback mForumDiffCallback = new ProfilePhotoDiffCallback();

    public void addToTop(Media media) {
        mItems.add(0, media);
        notifyItemInserted(0);
    }

    public void addAllToTop(List<Media> medias) {
        for (Media m : medias) {
            mItems.add(0, m);
            notifyItemInserted(0);
        }
    }

    public void addAllToEnd(List<Media> medias) {
        // record this value before making any changes to the existing list
        final int curSize = getDataItemCount();

        //Timber.d("addAllToEnd(); Old data size: %s", getDataItemCount());
        //Timber.d("addAllToEnd(); New data size: %s", posts.size());

        mItems.addAll(medias);
        notifyItemRangeInserted(curSize, getDataItemCount());

        //Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    public void refreshData(final List<Media> medias) {
        Timber.d("Data is coming from pullToRefresh!");

        mForumDiffCallback.setOldList(mItems);
        mForumDiffCallback.setNewList(medias);

        calculateDiff(mForumDiffCallback)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(medias);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }
}
