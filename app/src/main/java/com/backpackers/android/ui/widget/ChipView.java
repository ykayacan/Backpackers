package com.backpackers.android.ui.widget;

import com.backpackers.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChipView extends FrameLayout {

    @BindView(R.id.text_hashTag)
    TextView mHashTagTv;

    private OnDismissPreviewListener mListener;

    public ChipView(Context context) {
        super(context);
        init();
    }

    public ChipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.merge_chip_view, this);
        ButterKnife.bind(this);

        mHashTagTv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= mHashTagTv.getRight() - mHashTagTv.getTotalPaddingRight()) {
                        // your action for drawable click event
                        mListener.onDismissPreview(v);
                        return true;
                    }
                }
                return true;
            }
        });
    }

    public void setHashTag(String hashTag) {
        mHashTagTv.setText(getResources().getString(R.string.label_hashtag, hashTag));
    }

    public void setListener(OnDismissPreviewListener listener) {
        mListener = listener;
    }

    public interface OnDismissPreviewListener {
        void onDismissPreview(View view);
    }
}
