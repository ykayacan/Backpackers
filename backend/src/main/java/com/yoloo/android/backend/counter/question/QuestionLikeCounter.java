package com.yoloo.android.backend.counter.question;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.question.QuestionCounter;

public final class QuestionLikeCounter implements Count {

    private final QuestionCounter counter;

    public QuestionLikeCounter(QuestionCounter counter) {
        this.counter = counter;
    }

    @Override
    public void increase() {
        counter.setLikesCount(counter.getLikesCount() + 1);
    }

    @Override
    public void decrease() {
        if (counter.getLikesCount() >= 0) {
            counter.setLikesCount(counter.getLikesCount() - 1);
        }
    }
}
