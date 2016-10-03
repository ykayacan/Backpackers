package com.backpackers.android.ui.widget;

import com.backpackers.android.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageButton;

/**
 * An extension to {@link ImageButton} which implements the {@link Checkable} interface.
 */
public class CheckableImageButton extends ImageButton implements
        Checkable, View.OnClickListener {

    private static final int[] CHECKED_STATE_SET = {R.attr.isChecked};

    private boolean mIsChecked;
    private boolean mBroadcasting;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckableImageButton(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableImageButton);
        mIsChecked = a.getBoolean(R.styleable.CheckableImageButton_isChecked, false);
        setChecked(mIsChecked);
        a.recycle();

        setOnClickListener(this);
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mIsChecked != checked) {
            mIsChecked = checked;
            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            /*if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mIsChecked);
            }

            mBroadcasting = false;*/
        }
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }

    @Override // borrowed from CompoundButton#performClick()
    public boolean performClick() {
        toggle();
        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        return handled;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mIsChecked);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the checked state changed.
     */
    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state has changed.
         *
         * @param button    The button view whose state has changed.
         * @param isChecked The new checked state of button.
         */
        void onCheckedChanged(CheckableImageButton button, boolean isChecked);
    }
}
