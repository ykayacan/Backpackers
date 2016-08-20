package com.yoloo.android.ui.signin;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.repository.AccountRepository;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class SignInPresenter extends MvpBasePresenter<SignInView> {

    private final AccountRepository mRepository;
    private Subscription mSubscription;

    SignInPresenter(AccountRepository repository) {
        mRepository = repository;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    void sendGoogleToken(final String token) {
        if (isViewAttached()) {
            mSubscription = mRepository.add(token, "google")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Account>() {
                        @Override
                        public void onCompleted() {
                            getView().onNavigateToHome();
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO: 20.08.2016 Implement error check.
                            getView().onEmailExistsError();
                        }

                        @Override
                        public void onNext(Account account) {
                            getView().onSaveUser(account);
                        }
                    });
        }
    }

    void sendFacebookToken(final String token) {
        if (isViewAttached()) {
            mSubscription = mRepository.add(token, "facebook")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Account>() {
                        @Override
                        public void onCompleted() {
                            getView().onNavigateToHome();
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
