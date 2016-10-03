package com.backpackers.android.ui.widget;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create clickable spans within a TextView
 * made easy with mPattern matching!
 *
 * Created by: Nathan Esquenazi
 *
 * Usage 1: Apply spannable strings to a TextView based on mPattern
 *
 * new PatternEditableBuilder().
 * addPattern(Pattern.compile("\\@(\\w+)")).
 * into(textView);
 *
 * Usage 2: Apply clickable spans to a TextView
 *
 * new PatternEditableBuilder().
 * addPattern(Pattern.compile("\\@(\\w+)"), Color.BLUE,
 * new PatternEditableBuilder.SpannableClickedListener() {
 *
 * @Override public void onSpanClicked(String text) {
 * // Do something here
 * }
 * }).into(textView);
 *
 * See README for more details.
 */

public class PatternEditableBuilder {

    // Records the pattern spans to apply to a TextView
    private ArrayList<SpannablePatternItem> mPatterns;

    /* ----- Constructors ------- */
    public PatternEditableBuilder() {
        mPatterns = new ArrayList<>(5);
    }

    public PatternEditableBuilder addPattern(Pattern pattern) {
        addPattern(pattern, null, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern,
                                             SpannableStyleListener styleListener) {
        addPattern(pattern, styleListener, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern,
                                             SpannableStyleListener styleListener,
                                             SpannableClickedListener clickedListener) {
        mPatterns.add(new SpannablePatternItem(pattern, styleListener, clickedListener));
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, int textColor) {
        addPattern(pattern, textColor, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, int textColor,
                                             SpannableClickedListener listener) {
        final SpannableStyleListener styles = new SpannableStyleListener(textColor) {
            @Override
            public void onSpanStyled(TextPaint ds) {
                ds.linkColor = this.spanTextColor;
                ds.setUnderlineText(false);
            }
        };
        addPattern(pattern, styles, listener);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, SpannableClickedListener listener) {
        addPattern(pattern, null, listener);
        return this;
    }

    // This builds the pattern span and applies to a TextView
    public void into(TextView textView) {
        final SpannableStringBuilder result = build(textView.getText());
        textView.setText(result, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    // This builds the pattern span into a `SpannableStringBuilder`
    // Requires a CharSequence to be passed in to be applied to
    public SpannableStringBuilder build(CharSequence editable) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(editable);
        for (SpannablePatternItem item : mPatterns) {
            Matcher matcher = item.pattern.matcher(ssb);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                StyledClickableSpan url = new StyledClickableSpan(item);
                ssb.setSpan(url, start, end, 0);
                StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);
                ssb.setSpan(mBoldSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return ssb;
    }

    /* This stores the click listener for a pattern item
       Used to handle clicks to a particular category of spans */
    public interface SpannableClickedListener {
        void onSpanClicked(String text);
    }

    /**
     * This stores the style listener for a pattern item
     * Used to style a particular category of spans
     */
    public static abstract class SpannableStyleListener {
        public int spanTextColor;

        public SpannableStyleListener() {
        }

        public SpannableStyleListener(int spanTextColor) {
            this.spanTextColor = spanTextColor;
        }

        public abstract void onSpanStyled(TextPaint ds);
    }

    /* BUILDER METHODS */

    /**
     * This stores a particular pattern item
     * complete with pattern, span styles, and click listener
     */
    public class SpannablePatternItem {
        public SpannableStyleListener styles;
        public Pattern pattern;
        public SpannableClickedListener listener;

        public SpannablePatternItem(Pattern pattern, SpannableStyleListener styles, SpannableClickedListener listener) {
            this.pattern = pattern;
            this.styles = styles;
            this.listener = listener;
        }
    }

    /**
     * This is the custom clickable span class used
     * to handle user clicks to our pattern spans
     * applying the styles and invoking click listener.
     */
    public class StyledClickableSpan extends ClickableSpan {
        private SpannablePatternItem item;

        public StyledClickableSpan(SpannablePatternItem item) {
            this.item = item;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (item.styles != null) {
                item.styles.onSpanStyled(ds);
            }
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
            //super.updateDrawState(ds);
        }

        @Override
        public void onClick(View widget) {
            if (item.listener != null) {
                TextView tv = (TextView) widget;
                Spanned span = (Spanned) tv.getText();
                int start = span.getSpanStart(this);
                int end = span.getSpanEnd(this);
                item.listener.onSpanClicked(span.toString().substring(start, end));
            }
            widget.invalidate();
        }
    }
}
