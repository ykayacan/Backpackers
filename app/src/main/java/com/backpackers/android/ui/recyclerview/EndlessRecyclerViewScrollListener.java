package com.backpackers.android.ui.recyclerview;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private int mPreviousTotal = 0;
    private boolean mLoading = true;
    private int mVisibleThreshold = -1;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    private boolean mIsOrientationHelperVertical;
    private OrientationHelper mOrientationHelper;

    private RecyclerView.LayoutManager mLayoutManager;

    private OnLoadMoreListener mOnLoadMoreListener;

    private final BaseAdapter<?> mAdapter;

    public EndlessRecyclerViewScrollListener(BaseAdapter<?> adapter,
                                             RecyclerView.LayoutManager layoutManager,
                                             OnLoadMoreListener listener) {
        mAdapter = adapter;
        mLayoutManager = layoutManager;
        mOnLoadMoreListener = listener;
    }

    private int findFirstVisibleItemPosition(RecyclerView recyclerView) {
        final View child = findOneVisibleChild(0, mLayoutManager.getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    private int findLastVisibleItemPosition(RecyclerView recyclerView) {
        final View child = findOneVisibleChild(recyclerView.getChildCount() - 1, -1, false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    private View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                                     boolean acceptPartiallyVisible) {
        if (mLayoutManager.canScrollVertically() != mIsOrientationHelperVertical
                || mOrientationHelper == null) {
            mIsOrientationHelperVertical = mLayoutManager.canScrollVertically();
            mOrientationHelper = mIsOrientationHelperVertical
                    ? OrientationHelper.createVerticalHelper(mLayoutManager)
                    : OrientationHelper.createHorizontalHelper(mLayoutManager);
        }

        final int start = mOrientationHelper.getStartAfterPadding();
        final int end = mOrientationHelper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = mLayoutManager.getChildAt(i);
            if (child != null) {
                final int childStart = mOrientationHelper.getDecoratedStart(child);
                final int childEnd = mOrientationHelper.getDecoratedEnd(child);
                if (childStart < end && childEnd > start) {
                    if (completelyVisible) {
                        if (childStart >= start && childEnd <= end) {
                            return child;
                        } else if (acceptPartiallyVisible && partiallyVisible == null) {
                            partiallyVisible = child;
                        }
                    } else {
                        return child;
                    }
                }
            }
        }
        return partiallyVisible;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mLayoutManager == null) {
            mLayoutManager = recyclerView.getLayoutManager();
        }

        // bail out if scrolling upward or already loading data
        if (dy < 0) {
            return;
        }

        int footerItemCount = mAdapter.isShouldLoadMore() ? mAdapter.getItemCount() : 0;

        //Timber.d("Footer item count: %s", footerItemCount);

        if (mVisibleThreshold == -1) {
            mVisibleThreshold = findLastVisibleItemPosition(recyclerView)
                    - findFirstVisibleItemPosition(recyclerView)
                    - footerItemCount;
        }

        mVisibleItemCount = recyclerView.getChildCount() - footerItemCount;
        mTotalItemCount = mLayoutManager.getItemCount() - footerItemCount;
        mFirstVisibleItem = findFirstVisibleItemPosition(recyclerView);

        // If it's still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (mLoading && (mTotalItemCount > mPreviousTotal)) {
            mLoading = false;
            mPreviousTotal = mTotalItemCount;
        }

        // If it isn't currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!mLoading && (mTotalItemCount - mVisibleItemCount)
                <= (mFirstVisibleItem + mVisibleThreshold)) {

            mOnLoadMoreListener.onLoadMore();

            mLoading = true;
        }
    }

    public void resetPageCount() {
        mPreviousTotal = 0;
        mLoading = true;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public int getTotalItemCount() {
        return mTotalItemCount;
    }

    public int getFirstVisibleItem() {
        return mFirstVisibleItem;
    }

    public int getVisibleItemCount() {
        return mVisibleItemCount;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
