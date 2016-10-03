package com.backpackers.android.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

public class SpannableTextView extends TextView {

    /**
     * Matches for all # and @ characters. Also includes Turkish characters.
     */
    private Pattern mPattern =
            Pattern.compile("([@][A-z-şğ]+)|([#][A-z-ğüşöçİĞÜŞÖÇ]+)|((?:(?:https?|ftp):\\/\\/|www\\.)[^\\s/$.?#].[^\\s]*)");

    public SpannableTextView(Context context) {
        super(context);
    }

    public SpannableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpannableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpannableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnUsernameClick {
        void onUsernameClick(String username);
    }

    public interface OnHashTagClick {
        void onHashTagClick(String hashTag);
    }

    private static final class ProfileSpan extends ClickableSpan {

        private final String mText;

        @ColorInt
        private int mColor;

        public ProfileSpan(String text, int color) {
            mText = text;
            mColor = color;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setColor(mColor);
        }

        @Override
        public void onClick(View widget) {

        }
    }
}
