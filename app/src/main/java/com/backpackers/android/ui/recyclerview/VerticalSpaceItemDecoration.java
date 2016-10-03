package com.backpackers.android.ui.recyclerview;

import com.backpackers.android.util.ScreenUtils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mVerticalSpaceHeight;

    public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
        mVerticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = ScreenUtils.dpToPx(mVerticalSpaceHeight);

        // Removes bottom spacing for last item
        /*if (parent.getChildAdapterPosition(view) != parent.getAdapter().getDataItemCount() - 1) {
            outRect.bottom = mVerticalSpaceHeight;
        }*/
    }
}
