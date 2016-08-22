package com.yoloo.android.ui.signin.yoloo;

import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.repository.TokenRepository;
import com.yoloo.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class YolooSignInPresenter extends BaseMvpPresenter<YolooSignInView> {

    private final TokenRepository mRepository;
    private Subscription mSubscription;

    public YolooSignInPresenter(TokenRepository mRepository) {
        this.mRepository = mRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    void signIn(String email, String password) {
        if (!isViewAttached()) {
            return;
        }

        getView().onShowProgress(true);

        mSubscription = mRepository.get(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Token>() {
                    @Override
                    public void onCompleted() {
                        getView().onShowProgress(false);
                        getView().onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().onShowProgress(false);
                        getView().onError(e);
                    }

                    @Override
                    public void onNext(Token token) {
                        getView().onSaveToken(token);
                    }
                });
    }
}
