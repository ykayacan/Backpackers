package com.backpackers.android.ui.widget;

import com.backpackers.android.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LikeButton extends FrameLayout implements View.OnClickListener {

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR =
            new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR =
            new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR =
            new OvershootInterpolator(4);

    @BindView(R.id.icon)
    ImageView mIcon;

    @BindView(R.id.dots)
    DotsView mDotsView;

    @BindView(R.id.circle)
    CircleView mCircleView;

    private OnLikeListener mListener;

    private int mDotPrimaryColor;
    private int mDotSecondaryColor;

    private int mCircleStartColor;
    private int mCircleEndColor;

    private boolean mIsChecked;

    private boolean isEnabled;

    private AnimatorSet mAnimatorSet;

    private Drawable mLikeDrawable;
    private Drawable mUnLikeDrawable;

    public LikeButton(Context context) {
        super(context, null);
        init(context, null, 0, 0);
    }

    public LikeButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs, 0, 0);
    }

    public LikeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LikeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View v = LayoutInflater.from(context).inflate(R.layout.merge_likeview, this, true);
        ButterKnife.bind(this, v);

        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.LikeButton, defStyleAttr, 0);

        mLikeDrawable = getDrawableFromResource(a, R.styleable.LikeButton_like_drawable);

        if (mLikeDrawable != null) {
            setLikeDrawable(mLikeDrawable);
        }

        mUnLikeDrawable = getDrawableFromResource(a, R.styleable.LikeButton_unlike_drawable);

        if (mUnLikeDrawable != null) {
            setUnlikeDrawable(mUnLikeDrawable);
        }

        if (mLikeDrawable == null && mUnLikeDrawable == null) {
            setLikeDrawableRes(R.drawable.ic_like_full_red_24dp);
            setUnlikeDrawableRes(R.drawable.ic_like_empty_grey_24dp);
        }

        mCircleStartColor = a.getColor(R.styleable.LikeButton_circle_start_color, 0);

        if (mCircleStartColor != 0) {
            mCircleView.setStartColor(mCircleStartColor);
        }

        mCircleEndColor = a.getColor(R.styleable.LikeButton_circle_end_color, 0);

        if (mCircleEndColor != 0) {
            mCircleView.setEndColor(mCircleEndColor);
        }

        mDotPrimaryColor = a.getColor(R.styleable.LikeButton_dots_primary_color, 0);
        mDotSecondaryColor = a.getColor(R.styleable.LikeButton_dots_secondary_color, 0);

        if (mDotPrimaryColor != 0 && mDotSecondaryColor != 0) {
            mDotsView.setColors(mDotPrimaryColor, mDotSecondaryColor);
        }

        setEnabled(a.getBoolean(R.styleable.LikeButton_is_enabled, true));
        boolean status = a.getBoolean(R.styleable.LikeButton_is_liked, false);
        setLiked(status);
        setOnClickListener(this);
        a.recycle();
    }

    private Drawable getDrawableFromResource(TypedArray a, int styleableIndexId) {
        int id = a.getResourceId(styleableIndexId, -1);

        return (id != -1) ? ContextCompat.getDrawable(getContext(), id) : null;
    }

    /**
     * This triggers the entire functionality of the button such as mIcon changes,
     * animations, listeners etc.
     */
    @Override
    public void onClick(View v) {

        if (!isEnabled) {
            return;
        }

        mIsChecked = !mIsChecked;

        mIcon.setImageDrawable(mIsChecked ? mLikeDrawable : mUnLikeDrawable);

        if (mListener != null) {
            if (mIsChecked) {
                mListener.onLiked(this);
            } else {
                mListener.onUnLiked(this);
            }
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        if (mIsChecked) {
            mIcon.animate().cancel();
            mIcon.setScaleX(0);
            mIcon.setScaleY(0);

            mCircleView.setInnerCircleRadiusProgress(0);
            mCircleView.setOuterCircleRadiusProgress(0);
            mDotsView.setCurrentProgress(0);

            mAnimatorSet = new AnimatorSet();

            ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(mCircleView, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
            outerCircleAnimator.setDuration(250);
            outerCircleAnimator.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(mCircleView, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
            innerCircleAnimator.setDuration(200);
            innerCircleAnimator.setStartDelay(200);
            innerCircleAnimator.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(mIcon, ImageView.SCALE_Y, 0.2f, 1f);
            starScaleYAnimator.setDuration(350);
            starScaleYAnimator.setStartDelay(250);
            starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(mIcon, ImageView.SCALE_X, 0.2f, 1f);
            starScaleXAnimator.setDuration(350);
            starScaleXAnimator.setStartDelay(250);
            starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(mDotsView, DotsView.DOTS_PROGRESS, 0, 1f);
            dotsAnimator.setDuration(900);
            dotsAnimator.setStartDelay(50);
            dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

            mAnimatorSet.playTogether(
                    outerCircleAnimator,
                    innerCircleAnimator,
                    starScaleYAnimator,
                    starScaleXAnimator,
                    dotsAnimator
            );

            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    mCircleView.setInnerCircleRadiusProgress(0);
                    mCircleView.setOuterCircleRadiusProgress(0);
                    mDotsView.setCurrentProgress(0);
                    mIcon.setScaleX(1);
                    mIcon.setScaleY(1);
                }
            });

            mAnimatorSet.start();
        }
    }

    /**
     * Used to trigger the scale animation that takes places on the
     * mIcon when the button is touched.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*
                Commented out this line and moved the animation effect to the action up event due to
                conflicts that were occurring when library is used in sliding type views.

                mIcon.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).setInterpolator(DECELERATE_INTERPOLATOR);
                */

                setPressed(true);
                break;

            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                boolean isInside = (x > 0 && x < getWidth() && y > 0 && y < getHeight());
                if (isPressed() != isInside) {
                    setPressed(isInside);
                }
                break;

            case MotionEvent.ACTION_UP:
                mIcon.animate().scaleX(0.7f).scaleY(0.7f).setDuration(150).setInterpolator(DECELERATE_INTERPOLATOR);
                mIcon.animate().scaleX(1).scaleY(1).setInterpolator(DECELERATE_INTERPOLATOR);
                if (isPressed()) {
                    performClick();
                    setPressed(false);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                mIcon.animate().scaleX(1).scaleY(1).setInterpolator(DECELERATE_INTERPOLATOR);
                setPressed(false);
                break;
        }
        return true;
    }

    /**
     * This drawable is shown when the button is a onLiked state.
     */
    public void setLikeDrawableRes(@DrawableRes int resId) {
        mLikeDrawable = ContextCompat.getDrawable(getContext(), resId);
        mIcon.setImageDrawable(mLikeDrawable);
    }

    /**
     * This drawable is shown when the button is in a onLiked state.
     */
    public void setLikeDrawable(Drawable likeDrawable) {
        mLikeDrawable = likeDrawable;
        mIcon.setImageDrawable(mLikeDrawable);
    }

    /**
     * This drawable will be shown when the button is in on onUnLiked state.
     */
    public void setUnlikeDrawableRes(@DrawableRes int resId) {
        mUnLikeDrawable = ContextCompat.getDrawable(getContext(), resId);
        mIcon.setImageDrawable(mUnLikeDrawable);
    }

    /**
     * This drawable will be shown when the button is in on onUnLiked state.
     */
    public void setUnlikeDrawable(Drawable unLikeDrawable) {
        mUnLikeDrawable = unLikeDrawable;
        mIcon.setImageDrawable(unLikeDrawable);
    }

    /**
     * Listener that is triggered once the
     * button is in a onLiked or onUnLiked state
     */
    public void setOnLikeListener(OnLikeListener likeListener) {
        mListener = likeListener;
    }

    /**
     * This set sets the colours that are used for the little dots
     * that will be exploding once the like button is clicked.
     */
    public void setExplodingDotColorsRes(@ColorRes int primaryColor, @ColorRes int secondaryColor) {
        mDotsView.setColors(ContextCompat.getColor(getContext(), primaryColor),
                ContextCompat.getColor(getContext(), secondaryColor));
    }

    public void setCircleStartColorRes(@ColorRes int circleStartColor) {
        mCircleStartColor = circleStartColor;
        mCircleView.setStartColor(ContextCompat.getColor(getContext(), circleStartColor));
    }

    public void setCircleEndColorRes(@ColorRes int circleEndColor) {
        this.mCircleEndColor = circleEndColor;
        mCircleView.setEndColor(ContextCompat.getColor(getContext(), circleEndColor));
    }

    /**
     * Sets the initial state of the button to onLiked
     * or unliked.
     */
    public void setLiked(boolean status) {
        if (status) {
            mIsChecked = true;
            mIcon.setImageDrawable(mLikeDrawable);
        } else {
            mIsChecked = false;
            mIcon.setImageDrawable(mUnLikeDrawable);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public interface OnLikeListener {
        void onLiked(LikeButton likeButton);

        void onUnLiked(LikeButton likeButton);
    }

}
