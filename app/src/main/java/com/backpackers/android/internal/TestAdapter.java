package com.backpackers.android.internal;

import com.backpackers.android.ui.recyclerview.AdapterDelegate;

import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> mItems = new ArrayList<>(20);

    /**
     * Map for ViewType to AdapterDelegate
     */
    private SparseArrayCompat<TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder>> mDelegates =
            new SparseArrayCompat<>(3);

    /**
     * Add.
     *
     * @param item the item
     */
    public void add(Object item) {
        mItems.add(item);
        notifyItemInserted(getItemCount());
    }

    /**
     * Remove.
     *
     * @param item the item
     */
    public void remove(Object item) {
        final int pos = mItems.indexOf(item);
        mItems.remove(pos);
        notifyItemRemoved(pos);
    }

    /**
     * Add all to end.
     *
     * @param items the items
     */
    public void addAllToEnd(List<?> items) {
        // record this value before making any changes to the existing list
        final int curSize = getItemCount();

        Timber.d("addAllToEnd(); Old data size: %s", getItemCount());
        Timber.d("addAllToEnd(); New data size: %s", getItemCount());

        mItems.addAll(items);
        notifyItemRangeInserted(curSize, getItemCount());

        Timber.d("addAllToEnd(); Renewed data size: %s", getItemCount());
    }

    /**
     * Refresh data.
     *
     * @param items    the items
     * @param callback the callback
     */
    public void refreshData(final List<?> items, final DiffUtil.Callback callback) {
        Timber.d("Data is coming from pullToRefresh!");

        Observable.just(DiffUtil.calculateDiff(callback))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(items);

                        diffResult.dispatchUpdatesTo(getAdapter());
                    }
                });
    }

    /**
     * Add delegate.
     *
     * @param delegate the delegate
     */
    public void addDelegate(TestAbsAdapterDelegate<?, ? extends RecyclerView.ViewHolder> delegate) {
        delegate.setAdapter(this);
        mDelegates.append(delegate.getLayoutResId(), (TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder>) delegate);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder> delegate =
                getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        return delegate.onCreateViewHolder(parent, view, mItems);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            final TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder> delegate =
                    getDelegateForViewType(holder.getItemViewType());
            if (delegate == null) {
                throw new NullPointerException("No delegate found for item at position = "
                        + position
                        + " for viewType = "
                        + holder.getItemViewType());
            }
            delegate.onBindViewHolder(holder, getItem(position), position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder> delegate =
                getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onBindViewHolder(holder, getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        final int size = mDelegates.size();
        TestAbsAdapterDelegate<Object, ?> delegate;

        for (int i = size - 1; i >= 0; i--) {
            delegate = mDelegates.valueAt(i);
            if (delegate.isForViewType(getItem(position), position)) {
                return delegate.getLayoutResId();
            }
        }

        throw new NullPointerException(
                "No AdapterDelegate found that matches position = " + position + " in map.");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private Object getItem(int position) {
        return mItems.get(position);
    }

    public List<Object> getItems() {
        return mItems;
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return this;
    }

    /**
     * Get the {@link AdapterDelegate} associated with the given view type integer
     *
     * @param viewType The view type integer we want to retrieve the associated
     *                 delegate for.
     * @return The {@link AdapterDelegate} associated with the view type param if it exists,
     * the fallback delegate otherwise if it is set or returns <code>null</code> if no delegate is
     * associated to this viewType (and no fallback has been set).
     */
    @Nullable
    private TestAbsAdapterDelegate<Object, RecyclerView.ViewHolder> getDelegateForViewType(int viewType) {
        return mDelegates.get(viewType);
    }
}
