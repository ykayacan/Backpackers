package com.backpackers.android.ui.comment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

public class CommentPopup extends PopupWindow {

    public CommentPopup(Context context) {
        super(context);
    }

    public CommentPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CommentPopup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CommentPopup() {
    }

    public CommentPopup(View contentView) {
        super(contentView);
    }

    public CommentPopup(int width, int height) {
        super(width, height);
    }

    public CommentPopup(View contentView, int width, int height) {
        super(contentView, width, height);
    }

    public CommentPopup(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }
}
