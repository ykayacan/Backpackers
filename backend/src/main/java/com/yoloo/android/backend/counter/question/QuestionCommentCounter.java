package com.yoloo.android.backend.counter.question;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.question.QuestionCounter;

public final class QuestionCommentCounter implements Count {

    private final QuestionCounter counter;

    public QuestionCommentCounter(QuestionCounter counter) {
        this.counter = counter;
    }

    @Override
    public void increase() {
        counter.setCommentsCount(counter.getCommentsCount() + 1);
    }

    @Override
    public void decrease() {
        if (counter.getCommentsCount() >= 0) {
            counter.setCommentsCount(counter.getCommentsCount() - 1);
        }
    }
}
