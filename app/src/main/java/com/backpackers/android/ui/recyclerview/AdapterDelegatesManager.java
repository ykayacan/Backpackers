/*
 * Copyright (c) 2015 Hannes Dorfmann.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.backpackers.android.ui.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * The type Adapter delegates manager.
 *
 * @param <M> The type of the datasource of the adapter
 * @author Hannes Dorfmann
 */
public class AdapterDelegatesManager<M> {

    /**
     * Map for ViewType to AdapterDelegate
     */
    private SparseArrayCompat<AdapterDelegate<M, ? extends RecyclerView.ViewHolder>> mDelegates =
            new SparseArrayCompat<>(3);

    /**
     * Adds an {@link AdapterDelegate}.
     * <b>This method automatically assign internally the view type integer by using the next
     * unused</b>
     *
     * @param delegate the delegate to addForumComment
     * @return self
     * @throws NullPointerException if passed delegate is null
     */
    public AdapterDelegatesManager<M> add(@NonNull AdapterDelegate<M, ?> delegate) {
        mDelegates.put(delegate.getLayoutResId(), delegate);
        return this;
    }

    /**
     * Removes a previously registered delegate if and only if the passed delegate is registered
     * (checks the reference of the object). This will not remove any other delegate for the same
     * viewType (if there is any).
     *
     * @param delegate The delegate to remove
     * @return self
     */
    public AdapterDelegatesManager<M> remove(@NonNull AdapterDelegate<M, ?> delegate) {
        int index = mDelegates.indexOfValue(delegate);
        mDelegates.removeAt(index);
        return this;
    }

    /**
     * Removes the adapterDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public AdapterDelegatesManager<M> remove(int viewType) {
        mDelegates.remove(viewType);
        return this;
    }

    /**
     * Must be called from {@link RecyclerView.Adapter#getItemViewType(int)}. Internally it scans
     * all
     * the registered {@link AdapterDelegate} and picks the right one to return the ViewType
     * integer.
     *
     * @param items    Adapter's data source
     * @param position the position in adapters data source
     * @throws IllegalArgumentException if no {@link AdapterDelegate} has been found that is
     *                                  responsible for the given data element in data set (No
     *                                  {@link AdapterDelegate} for the given
     *                                  ViewType)
     * @throws NullPointerException     if items is null
     */
    public int getItemViewType(@NonNull List<M> items, int position) {
        final int delegatesCount = mDelegates.size() - 1;
        AdapterDelegate<M, ?> delegate;
        M item;

        for (int i = delegatesCount; i >= 0; i--) {
            delegate = mDelegates.valueAt(i);
            item = items.get(position);
            if (delegate.isForViewType(item, position)) {
                return delegate.getLayoutResId();
            }
        }

        throw new NullPointerException(
                "No AdapterDelegate added that matches position = " + position + " in data source");
    }

    /**
     * This method must be called in {@link RecyclerView.Adapter#onCreateViewHolder(ViewGroup,
     * int)}
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return The new created ViewHolder
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType, List<M> items) {
        final AdapterDelegate<M, ?> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return delegate.onCreateViewHolder(parent, view, items);
    }

    /**
     * Must be called from{@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder,
     * int)}
     *
     * @param items    Adapter's data source
     * @param position the position in data source
     * @param holder   the ViewHolder to bind
     * @throws NullPointerException if no AdapterDelegate has been registered for ViewHolders
     *                              viewType
     */
    public void onBindViewHolder(@NonNull List<M> items, @NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        final AdapterDelegate<M, ? super RecyclerView.ViewHolder> delegate =
                getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        final M item = items.get(position);
        delegate.onBindViewHolder(item, holder, position);
    }

    /**
     * Get the view type integer for the given {@link AdapterDelegate}
     *
     * @param delegate The delegate we want to know the view type for
     * @return -1 if passed delegate is unknown, otherwise the view type integer
     */
    public int getViewType(@NonNull AdapterDelegate<M, ? extends RecyclerView.ViewHolder> delegate) {
        final int index = mDelegates.indexOfValue(delegate);
        return mDelegates.keyAt(index);
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
    @SuppressWarnings("unchecked")
    private <VH extends RecyclerView.ViewHolder> AdapterDelegate<M, VH> getDelegateForViewType(int viewType) {
        return (AdapterDelegate<M, VH>) mDelegates.get(viewType);
    }
}
