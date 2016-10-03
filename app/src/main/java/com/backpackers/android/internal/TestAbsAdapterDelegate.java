package com.backpackers.android.internal;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * The type Test abs adapter delegate.
 *
 * @param <M>  the type parameter
 * @param <VH> the type parameter
 */
public abstract class TestAbsAdapterDelegate<M, VH extends RecyclerView.ViewHolder> {

    private RecyclerView.Adapter<?> mAdapter;

    /**
     * Gets adapter.
     *
     * @return the adapter
     */
    public final RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    /**
     * Sets adapter.
     *
     * @param adapter the adapter
     */
    public final void setAdapter(RecyclerView.Adapter<?> adapter) {
        mAdapter = adapter;
    }

    /**
     * Gets layout res id.
     *
     * @return the layout res id
     */
    @LayoutRes
    protected abstract int getLayoutResId();

    /**
     * Called to determine whether this AdapterDelegate is the responsible for the given data
     * element.
     *
     * @param item     The data source of the Adapter
     * @param position The position in the data source
     * @return true, if this item is responsible,  otherwise false
     */
    protected abstract boolean isForViewType(@NonNull Object item, int position);

    /**
     * Creates the  {@link RecyclerView.ViewHolder} for the given data source item
     *
     * @param parent The ViewGroup parent of the given data source
     * @param view   the view
     * @param items   the items
     * @return The new instantiated {@link RecyclerView.ViewHolder}
     */
    @NonNull
    protected abstract VH onCreateViewHolder(ViewGroup parent, View view, List<M> items);

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the data source set
     *
     * @param holder   The {@link RecyclerView.ViewHolder} to bind
     * @param item     The data source
     * @param position The position in the data source
     */
    protected abstract void onBindViewHolder(VH holder, M item, int position);

    /**
     * Called to bind the {@link RecyclerView.ViewHolder} to the item of the data source set
     *
     * @param holder   The {@link RecyclerView.ViewHolder} to bind
     * @param item     the item
     * @param position The position in the data source
     * @param payloads the payloads
     */
    protected abstract void onBindViewHolder(VH holder, M item, int position, List<Object> payloads);
}
