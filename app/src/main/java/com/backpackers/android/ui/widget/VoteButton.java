package com.backpackers.android.ui.widget;

import com.backpackers.android.R;
import com.backpackers.android.util.DrawableHelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class VoteButton extends ImageButton implements View.OnClickListener {

    private boolean mIsChecked = false;

    private Drawable mVoteUpDrawable;
    private Drawable mVoteDownDrawable;

    private OnVotedUpListener mOnVotedUpListener;
    private OnVotedDownListener mOnVotedDownListener;

    public VoteButton(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public VoteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.VoteButton, defStyleAttr, 0);

        mVoteUpDrawable = getDrawableFromResource(a, R.styleable.VoteButton_up_drawable);
        mVoteDownDrawable = getDrawableFromResource(a, R.styleable.VoteButton_down_drawable);

        if (mVoteUpDrawable != null && mVoteDownDrawable != null) {
            throw new RuntimeException("Choose only one of it.");
        }

        setOnClickListener(this);

        a.recycle();
    }

    private Drawable getDrawableFromResource(TypedArray a, int styleableIndexId) {
        int id = a.getResourceId(styleableIndexId, -1);

        return (id != -1) ? AppCompatResources.getDrawable(getContext(), id) : null;
    }

    @Override
    public void onClick(View v) {
        mIsChecked = !mIsChecked;

        int color = 0;
        if (mVoteUpDrawable != null) {
            color = mIsChecked ? R.color.green_special : android.R.color.secondary_text_dark;
        } else if (mVoteDownDrawable != null) {
            color = mIsChecked ? R.color.red_special : android.R.color.secondary_text_dark;
        }

        Drawable drawable = null;
        if (mOnVotedUpListener != null && mVoteUpDrawable != null) {
            drawable = mVoteUpDrawable;
            if (mIsChecked) {
                mOnVotedUpListener.onVotedUp();
            } else {
                mOnVotedUpListener.onDefault();
            }
        } else if (mOnVotedDownListener != null && mVoteDownDrawable != null) {
            drawable = mVoteDownDrawable;
            if (mIsChecked) {
                mOnVotedDownListener.onVotedDown();
            } else {
                mOnVotedDownListener.onDefault();
            }
        }

        if (drawable != null) {
            DrawableHelper.withContext(getContext())
                    .withColor(color)
                    .withDrawable(drawable)
                    .tint()
                    .applyTo(this);
        }
    }

    public void setVotedUp() {
        if (mVoteUpDrawable != null) {
            mIsChecked = true;

            DrawableHelper.withContext(getContext())
                    .withColor(R.color.green_special)
                    .withDrawable(mVoteUpDrawable)
                    .tint()
                    .applyTo(this);
        }
    }

    public void setVotedDown() {
        if (mVoteDownDrawable != null) {
            mIsChecked = true;

            DrawableHelper.withContext(getContext())
                    .withColor(R.color.red_special)
                    .withDrawable(mVoteDownDrawable)
                    .tint()
                    .applyTo(this);
        }
    }

    public void setVotedDefault() {
        mIsChecked = false;

        setUpToDefault();
        setDownToDefault();
    }

    public void setUpToDefault() {
        if (mVoteUpDrawable != null) {
            DrawableHelper.withContext(getContext())
                    .withColor(android.R.color.secondary_text_dark)
                    .withDrawable(mVoteUpDrawable)
                    .tint()
                    .applyTo(this);
        }
    }

    public void setDownToDefault() {
        if (mVoteDownDrawable != null) {
            DrawableHelper.withContext(getContext())
                    .withColor(android.R.color.secondary_text_dark)
                    .withDrawable(mVoteDownDrawable)
                    .tint()
                    .applyTo(this);
        }
    }

    public boolean isUp() {
        return mVoteUpDrawable != null && mIsChecked;
    }

    public boolean isDown() {
        return mVoteDownDrawable != null && mIsChecked;
    }

    public void setOnVotedUpListener(OnVotedUpListener onVotedUpListener) {
        mOnVotedUpListener = onVotedUpListener;
    }

    public void setOnVotedDownListener(OnVotedDownListener onVotedDownListener) {
        mOnVotedDownListener = onVotedDownListener;
    }

    public interface OnVotedUpListener {
        void onVotedUp();

        void onDefault();
    }

    public interface OnVotedDownListener {
        void onVotedDown();

        void onDefault();
    }

}
