package com.backpackers.android.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * Created by froger_mcs on 21/03/16.
 */
public class KeyboardWatcher {

    private WeakReference<Activity> mActivityRef;
    private WeakReference<View> mRootViewRef;
    private WeakReference<OnKeyboardToggleListener> mOnKeyboardToggleListenerRef;
    private ViewTreeObserver.OnGlobalLayoutListener mViewTreeObserverListener;

    public KeyboardWatcher(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
        initialize();
    }

    public void setListener(OnKeyboardToggleListener onKeyboardToggleListener) {
        mOnKeyboardToggleListenerRef = new WeakReference<>(onKeyboardToggleListener);
    }

    public void destroy() {
        if (mRootViewRef.get() != null) {
            mRootViewRef.get().getViewTreeObserver().removeOnGlobalLayoutListener(mViewTreeObserverListener);
        }
    }

    private void initialize() {
        if (hasAdjustResizeInputMode()) {
            mViewTreeObserverListener = new GlobalLayoutListener();
            mRootViewRef = new WeakReference<>(mActivityRef.get().findViewById(Window.ID_ANDROID_CONTENT));
            mRootViewRef.get().getViewTreeObserver().addOnGlobalLayoutListener(mViewTreeObserverListener);
        } else {
            throw new IllegalArgumentException(
                    String.format("Activity %s should have windowSoftInputMode=\"adjustResize\"" +
                                    "to make KeyboardWatcher working. You can set it in AndroidManifest.xml",
                            mActivityRef.get().getClass().getSimpleName()));
        }
    }

    private boolean hasAdjustResizeInputMode() {
        return (mActivityRef.get().getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) != 0;
    }

    public interface OnKeyboardToggleListener {
        void onKeyboardShown(int keyboardSize);

        void onKeyboardClosed();
    }

    private class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        int initialValue;
        boolean hasSentInitialAction;
        boolean isKeyboardShown;

        @Override
        public void onGlobalLayout() {
            if (initialValue == 0) {
                initialValue = mRootViewRef.get().getHeight();
            } else {
                if (initialValue > mRootViewRef.get().getHeight()) {
                    if (mOnKeyboardToggleListenerRef.get() != null) {
                        if (!hasSentInitialAction || !isKeyboardShown) {
                            isKeyboardShown = true;
                            mOnKeyboardToggleListenerRef.get().onKeyboardShown(initialValue - mRootViewRef.get().getHeight());
                        }
                    }
                } else {
                    if (!hasSentInitialAction || isKeyboardShown) {
                        isKeyboardShown = false;
                        mRootViewRef.get().post(new Runnable() {
                            @Override
                            public void run() {
                                if (mOnKeyboardToggleListenerRef.get() != null) {
                                    mOnKeyboardToggleListenerRef.get().onKeyboardClosed();
                                }
                            }
                        });
                    }
                }
                hasSentInitialAction = true;
            }
        }
    }
}
