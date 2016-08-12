package com.yoloo.android.ui.question;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.yoloo.android.backend.modal.yolooApi.model.Question;
import com.yoloo.android.data.model.QuestionModel;
import com.yoloo.android.data.remote.QuestionService;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class QuestionPresenter extends MvpBasePresenter<QuestionView> {

    private QuestionService mQuestionService;
    private Subscription mSubscription;

    public QuestionPresenter(QuestionService questionService) {
        mQuestionService = questionService;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    public void send(QuestionModel model) {
        mSubscription = mQuestionService.create(model)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Question>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Done");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Question question) {

                    }
                });
    }
}
