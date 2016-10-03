package com.backpackers.android.ui.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class AdapterManager<M> {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;

    private String mOwnerId;

    public AdapterManager(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter,
                          String ownerId) {
        mAdapter = adapter;
        mOwnerId = ownerId;
    }

    /**
     * Map for ViewType to AdapterDelegate
     */
    private SparseArrayCompat<AbsAdapterDelegate<M, ?>> mDelegates
            = new SparseArrayCompat<>(5);

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return mAdapter;
    }

    /**
     * Adds an {@link AdapterDelegate}.
     * <b>This method automatically assign internally the view type integer by using the next
     * unused</b>
     *
     * @param delegate the delegate to addForumComment
     * @return self
     * @throws NullPointerException if passed delegate is null
     */
    public AdapterManager<M> add(@NonNull AbsAdapterDelegate<M, ?> delegate) {
        delegate.setAdapter(mAdapter);
        delegate.setOwnerId(mOwnerId);
        mDelegates.put(delegate.getLayoutResId(), delegate);
        return this;
    }

    /**
     * Removes the adapterDelegate for the given view types.
     *
     * @param viewType The Viewtype
     * @return self
     */
    public AdapterManager<M> remove(int viewType) {
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
        final int delegatesCount = mDelegates.size();
        AbsAdapterDelegate<M, ?> delegate;
        M item = items.get(position);

        for (int i = delegatesCount - 1; i >= 0; i--) {
            // Search for matching delegate.
            delegate = mDelegates.valueAt(i);
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
        final AbsAdapterDelegate<M, ?> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No AdapterDelegate added for ViewType " + viewType);
        }
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        //final RecyclerView.ViewHolder vh = delegate
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
    public void onBindViewHolder(@NonNull List<M> items,
                                 @NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        final AbsAdapterDelegate<M, ? super RecyclerView.ViewHolder> delegate =
                getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }

        delegate.onBindViewHolder(items.get(position),  holder, position);
    }

    /**
     * Get the view type integer for the given {@link AdapterDelegate}
     *
     * @param delegate The delegate we want to know the view type for
     * @return -1 if passed delegate is unknown, otherwise the view type integer
     */
    public int getViewType(@NonNull AbsAdapterDelegate<M, ?> delegate) {
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
    private AbsAdapterDelegate<M, ? super RecyclerView.ViewHolder> getDelegateForViewType(int viewType) {
        return (AbsAdapterDelegate<M, ? super RecyclerView.ViewHolder>) mDelegates.get(viewType);
    }
}
