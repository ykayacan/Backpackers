package com.yoloo.android.ui.signin.providers;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.repository.AccountRepository;
import com.yoloo.android.framework.base.BaseMvpPresenter;
import com.yoloo.android.ui.signin.AuthView;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class SignInPresenter extends BaseMvpPresenter<AuthView> {

    private final AccountRepository mRepository;
    private Subscription mSubscription;

    SignInPresenter(AccountRepository repository) {
        mRepository = repository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    void sendGoogleToken(final String token) {
        if (!isViewAttached()) {
            return;
        }

        mSubscription = mRepository.add(token, "google")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {
                        getView().onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO: 20.08.2016 Implement error check.
                        getView().onError(e);
                    }

                    @Override
                    public void onNext(Account account) {
                        getView().onSaveUser(account);
                        getView().onSaveToken(new Token().setAccessToken(token));
                    }
                });
    }

    void sendFacebookToken(final String token) {
        if (isViewAttached()) {
            mSubscription = mRepository.add(token, "facebook")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Account>() {
                        @Override
                        public void onCompleted() {
                            getView().onSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO: 20.08.2016 Implement error check.
                        }

                        @Override
                        public void onNext(Account account) {
                            getView().onSaveUser(account);
                        }
                    });
        }
    }
}
