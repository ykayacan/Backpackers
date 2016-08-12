package com.yoloo.android.backend.counter.user;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.user.UserIndexShard;

public final class UserQuestionCounter implements Count {

    private final UserIndexShard counter;

    public UserQuestionCounter(UserIndexShard counter) {
        this.counter = counter;
    }

    @Override
    public void increase() {
        counter.setQuestionCount(counter.getQuestionCount() + 1);
    }

    @Override
    public void decrease() {
        if (counter.getQuestionCount() >= 0) {
            counter.setQuestionCount(counter.getQuestionCount() - 1);
        }
    }
}
