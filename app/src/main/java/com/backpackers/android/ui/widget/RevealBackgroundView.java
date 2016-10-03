package com.backpackers.android.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class RevealBackgroundView extends View {

    public static final int STATE_NOT_STARTED = 0;
    public static final int STATE_FILL_STARTED = 1;
    public static final int STATE_FINISHED = 2;

    private static final int DEFAULT_PAINT_COLOR = Color.WHITE;

    private static final Interpolator INTERPOLATOR = new AccelerateInterpolator();
    private static final int FILL_TIME = 450;

    private int mState = STATE_NOT_STARTED;

    private Paint mFillPaint;
    private int mCurrentRadius;

    private int mStartLocationX;
    private int mStartLocationY;

    private OnStateChangeListener mOnStateChangeListener;

    public RevealBackgroundView(Context context) {
        super(context);
        init(null);
    }

    public RevealBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RevealBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RevealBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, com.backpackers.android.R.styleable.RevealBackgroundView);
        int revealColor = a.getColor(com.backpackers.android.R.styleable.RevealBackgroundView_revealColor, DEFAULT_PAINT_COLOR);

        a.recycle();

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(revealColor);
    }

    public void startFromLocation(int[] tapLocationOnScreen) {
        changeState(STATE_FILL_STARTED);
        mStartLocationX = tapLocationOnScreen[0];
        mStartLocationY = tapLocationOnScreen[1];

        final ObjectAnimator animator =
                ObjectAnimator.ofInt(this, "currentRadius", 0, (int) Math.hypot(getWidth(), getHeight()))
                        .setDuration(FILL_TIME);
        animator.setInterpolator(INTERPOLATOR);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFillPaint.setColor(DEFAULT_PAINT_COLOR);
                changeState(STATE_FINISHED);
            }
        });
        animator.start();
    }

    public void setToFinishedFrame() {
        changeState(STATE_FINISHED);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mState == STATE_FINISHED) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), mFillPaint);
        } else {
            canvas.drawCircle(mStartLocationX, mStartLocationY, mCurrentRadius, mFillPaint);
        }
    }

    private void changeState(int state) {
        if (mState == state) {
            return;
        }

        mState = state;
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.onStateChange(state);
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public void setCurrentRadius(int radius) {
        mCurrentRadius = radius;
        invalidate();
    }

    public interface OnStateChangeListener {
        void onStateChange(int state);
    }
}
