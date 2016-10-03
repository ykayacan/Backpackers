package com.backpackers.android.ui.recyclerview;

import com.backpackers.android.R;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public abstract class BaseAdapter<M> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<M> mItems = new ArrayList<>(20);

    private AdapterManager<M> mAdapterManager;

    private boolean mShouldLoadMore = false;

    public final void setAdapterManager(AdapterManager<M> adapterManager) {
        mAdapterManager = adapterManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == R.layout.item_loading_footer) {
            View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new FooterViewHolder(v);
        } else {
            return mAdapterManager.onCreateViewHolder(parent, viewType, mItems);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) != R.layout.item_loading_footer) {
            mAdapterManager.onBindViewHolder(mItems, holder, position);
        } else {
            ((FooterViewHolder) holder).mProgressBar.setVisibility(position > 0
                    ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((position < getDataItemCount() && getDataItemCount() > 0)) {
            return mAdapterManager.getItemViewType(mItems, position);
        }
        return R.layout.item_loading_footer;
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + (mShouldLoadMore ? 1 : 0);
    }

    public boolean isShouldLoadMore() {
        return mShouldLoadMore;
    }

    public void setShouldLoadMore(boolean shouldLoadMore) {
        if (shouldLoadMore) {
            dataStartedLoading();
        } else {
            dataFinishedLoading();
        }
    }

    private void dataStartedLoading() {
        if (mShouldLoadMore) {
            return;
        }
        mShouldLoadMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }

    private void dataFinishedLoading() {
        if (!mShouldLoadMore) {
            return;
        }
        final int loadingPos = getLoadingMoreItemPosition();
        mShouldLoadMore = false;
        notifyItemRemoved(loadingPos);
    }

    private int getLoadingMoreItemPosition() {
        return mShouldLoadMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    public final int getDataItemCount() {
        return mItems.size();
    }

    protected final Observable<DiffUtil.DiffResult> calculateDiff(DiffUtil.Callback callback) {
        return Observable.just(DiffUtil.calculateDiff(callback));
    }

    protected final RecyclerView.Adapter getAdapter() {
        return this;
    }
}
