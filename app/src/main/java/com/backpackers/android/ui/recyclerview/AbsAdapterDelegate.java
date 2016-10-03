package com.backpackers.android.ui.recyclerview;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbsAdapterDelegate<M, VH extends RecyclerView.ViewHolder> {

    private String mOwnerId;

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return mAdapter;
    }

    public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        mAdapter = adapter;
    }

    public void setOwnerId(String ownerId) {
        mOwnerId = ownerId;
    }

    protected final boolean isOwner(String itemOwnerId) {
        return mOwnerId.equals(itemOwnerId);
    }

    @LayoutRes
    protected abstract int getLayoutResId();

    /**
     * Called to determine whether this AdapterDelegate is the responsible for the given data
     * element.
     *
     * @param item     The data source of the Adapter
     * @param position The position in the datasource
     * @return true, if this item is responsible,  otherwise false
     */
    protected abstract boolean isForViewType(@NonNull M item, int position);

    /**
     * Creates the  {@link RecyclerView.ViewHolder} for the given data source item
     *
     * @param parent The ViewGroup parent of the given datasource
     * @param items
     * @return The new instantiated {@link RecyclerView.ViewHolder}
     */
    @NonNull
    protected abstract VH onCreateViewHolder(ViewGroup parent, View view, List<M> items);

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the datas source set
     *
     * @param item     The data source
     * @param holder   The {@link RecyclerView.ViewHolder} to bind
     * @param position The position in the datasource
     */
    protected abstract void onBindViewHolder(M item, VH holder, int position);
}
