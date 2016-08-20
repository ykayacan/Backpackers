package com.yoloo.android.ui.signin.yoloo;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.repository.TokenRepository;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class YolooSignInPresenter extends MvpBasePresenter<YolooSignInView> {

    private final TokenRepository mRepository;
    private Subscription mSubscription;

    public YolooSignInPresenter(TokenRepository mRepository) {
        this.mRepository = mRepository;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    void signIn(String email, String password) {
        if (isViewAttached()) {
            getView().onShowProgressDialog(true);

            mSubscription = mRepository.get(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Token>() {
                        @Override
                        public void onCompleted() {
                            getView().onShowProgressDialog(false);
                            getView().onNavigateToHome();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getView().onShowProgressDialog(false);
                            getView().onError();
                        }

                        @Override
                        public void onNext(Token token) {
                            getView().onSaveToken(token);
                        }
                    });
        }
    }
}
