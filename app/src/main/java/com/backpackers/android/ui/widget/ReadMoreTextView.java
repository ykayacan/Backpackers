/*
 * Copyright (C) 2016 Borja Bravo Álvarez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.backpackers.android.ui.widget;

import com.backpackers.android.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

public class ReadMoreTextView extends BaselineGridTextView {

    private static final int TRIM_MODE_LINES = 0;
    private static final int TRIM_MODE_LENGTH = 1;
    private static final int DEFAULT_TRIM_LENGTH = 240;
    private static final int DEFAULT_TRIM_LINES = 3;
    private static final int INVALID_END_INDEX = -1;
    private static final String ELLIPSIZE = "... ";

    private CharSequence text;
    private BufferType bufferType;
    private int trimLength;
    private CharSequence trimCollapsedText;
    private ReadMoreClickableSpan viewMoreSpan;
    private int colorClickableText;

    private int trimMode;
    private int lineEndIndex;
    private int trimLines;

    private OnReadMoreListener mOnReadMoreListener;

    public ReadMoreTextView(Context context) {
        super(context, null);

        init(context, null);
    }

    public ReadMoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView);

        this.trimLength = a.getInt(R.styleable.ReadMoreTextView_trimLength, DEFAULT_TRIM_LENGTH);
        int resourceIdTrimCollapsedText =
                a.getResourceId(R.styleable.ReadMoreTextView_trimCollapsedText, R.string.read_more);
        this.trimCollapsedText = getResources().getString(resourceIdTrimCollapsedText);
        this.trimLines = a.getInt(R.styleable.ReadMoreTextView_trimLines, DEFAULT_TRIM_LINES);
        this.colorClickableText = a.getColor(R.styleable.ReadMoreTextView_colorClickableText,
                ContextCompat.getColor(context, android.R.color.black));
        this.trimMode = a.getInt(R.styleable.ReadMoreTextView_trimMode, TRIM_MODE_LINES);
        a.recycle();

        viewMoreSpan = new ReadMoreClickableSpan();
        onGlobalLayoutLineEndIndex();
        setText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        this.text = text;
        bufferType = type;
        setText();
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
    }

    private CharSequence getDisplayableText() {
        return getTrimmedText(text);
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (trimMode == TRIM_MODE_LENGTH) {
            if (text != null && text.length() > trimLength) {
                return updateCollapsedText();
            }
        } else if (trimMode == TRIM_MODE_LINES) {
            if (text != null && lineEndIndex > 0) {
                return updateCollapsedText();
            }
        }
        return text;
    }

    private CharSequence updateCollapsedText() {
        try {
            int trimEndIndex = text.length();
            switch (trimMode) {
                case TRIM_MODE_LINES:
                    trimEndIndex = lineEndIndex - (ELLIPSIZE.length() + trimCollapsedText.length() + 1);
                    if (trimEndIndex < 0) {
                        trimEndIndex = trimLength + 1;
                    }
                    break;
                case TRIM_MODE_LENGTH:
                    trimEndIndex = trimLength + 1;
                    break;
            }

            if (text.length() > 20) {
                SpannableStringBuilder s = new SpannableStringBuilder(text, 0, trimEndIndex)
                        .append(ELLIPSIZE)
                        .append(trimCollapsedText);
                return addClickableSpan(s, trimCollapsedText);
            } else {
                return text;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return text;
        }
    }

    private CharSequence addClickableSpan(SpannableStringBuilder s, CharSequence trimText) {
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        s.setSpan(bss, s.length() - trimText.length(), s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(viewMoreSpan, s.length() - trimText.length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    private void onGlobalLayoutLineEndIndex() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                refreshLineEndIndex();
                setText();
            }
        });
    }

    private void refreshLineEndIndex() {
        try {
            if (trimLines == 0) {
                lineEndIndex = getLayout().getLineEnd(0);
            } else if (trimLines > 0 && getLineCount() >= trimLines) {
                lineEndIndex = getLayout().getLineEnd(trimLines - 1);
            } else {
                lineEndIndex = INVALID_END_INDEX;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnReadMoreListener(OnReadMoreListener onReadMoreListener) {
        mOnReadMoreListener = onReadMoreListener;
    }

    public interface OnReadMoreListener {
        void onReadMore(View v);
    }

    private class ReadMoreClickableSpan extends ClickableSpan {

        @Override
        public void onClick(View widget) {
            mOnReadMoreListener.onReadMore(widget);
            //mReadMore = !mReadMore;
            //setText();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(colorClickableText);
        }
    }
}