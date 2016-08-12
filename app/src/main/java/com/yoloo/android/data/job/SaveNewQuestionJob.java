package com.yoloo.android.data.job;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.yoloo.android.data.model.QuestionModel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SaveNewQuestionJob extends Job {

    private final QuestionModel mModel;

    protected SaveNewQuestionJob(QuestionModel model) {
        super(new Params(Priority.MID).requireNetwork().persist().groupBy("post_question"));
        mModel = model;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
