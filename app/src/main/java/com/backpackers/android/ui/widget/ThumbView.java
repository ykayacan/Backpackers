package com.backpackers.android.ui.widget;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.backpackers.android.R;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThumbView extends FrameLayout {

    @BindView(R.id.image_thumb_preview)
    ImageView mThumbPreview;

    private OnDismissPreviewListener mListener;

    public ThumbView(Context context) {
        super(context);
        init();
    }

    public ThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.merge_thumb_preview, this);
        ButterKnife.bind(this);
    }

    public void setThumbPreview(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mThumbPreview);
    }

    public void setListener(OnDismissPreviewListener listener) {
        mListener = listener;
    }

    @OnClick(R.id.image_btn_cancel)
    void dismiss(View v) {
        mListener.onDismissPreview(v);
    }

    public interface OnDismissPreviewListener {
        void onDismissPreview(View view);
    }
}
