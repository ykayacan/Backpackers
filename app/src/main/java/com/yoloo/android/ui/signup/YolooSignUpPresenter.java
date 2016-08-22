package com.yoloo.android.ui.signup;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.model.YolooError;
import com.yoloo.android.data.repository.AccountRepository;
import com.yoloo.android.data.repository.TokenRepository;
import com.yoloo.android.framework.base.BaseMvpPresenter;
import com.yoloo.android.util.ErrorUtil;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class YolooSignUpPresenter extends BaseMvpPresenter<YolooSignUpView> {

    private final AccountRepository mAccountRepository;
    private final TokenRepository mTokenRepository;
    private Subscription mAccountSubscription;
    private Subscription mTokenSubscription;

    public YolooSignUpPresenter(AccountRepository mAccountRepository,
                                TokenRepository mTokenRepository) {
        this.mAccountRepository = mAccountRepository;
        this.mTokenRepository = mTokenRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (mAccountSubscription != null && !mAccountSubscription.isUnsubscribed()) {
            mAccountSubscription.unsubscribe();
        }

        if (mTokenSubscription != null && !mTokenSubscription.isUnsubscribed()) {
            mTokenSubscription.unsubscribe();
        }
    }

    void signUp(final String username, final String email, final String password) {
        if (!isViewAttached()) {
            return;
        }

        getView().onShowProgress(true);

        mAccountSubscription = mAccountRepository.add(username, email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().onShowProgress(false);
                        YolooError error = ErrorUtil.parse(e);

                        if (error.getCode() == 409) {
                            if (error.getMessage().contains("Email")) {
                                getView().onEmailExistsError();
                            } else {
                                getView().onUsernameExistsError();
                            }
                        } else {
                            getView().onError(e);
                        }
                    }

                    @Override
                    public void onNext(Account account) {
                        getView().onSaveUser(account);
                        getAccessToken(email, password);
                    }
                });
    }

    private void getAccessToken(final String email, final String password) {
        mTokenSubscription = mTokenRepository.get(email, password)
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

                    }

                    @Override
                    public void onNext(Token token) {
                        getView().onSaveToken(token);
                    }
                });
    }
}
