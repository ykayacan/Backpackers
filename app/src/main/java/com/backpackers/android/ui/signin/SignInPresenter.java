package com.backpackers.android.ui.signin;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.TokenRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import android.support.v4.util.Pair;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SignInPresenter extends BaseMvpPresenter<SignInView> {

    private UserRepository mUserRepository;
    private TokenRepository mTokenRepository;

    private Subscription mUserSubscription;
    private Subscription mTokenSubscription;

    public SignInPresenter(UserRepository userRepository,
                           TokenRepository tokenRepository) {
        mUserRepository = userRepository;
        mTokenRepository = tokenRepository;
    }

    public SignInPresenter(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mUserSubscription, mTokenSubscription);
    }

    public void signUp(final String username, final String email,
                       final String password, final String locale) {
        if (!isViewAttached()) {
            return;
        }

        getView().onShowProgress(true, false);

        mUserSubscription = mUserRepository.add(username, email, password, locale)
                .flatMap(new Func1<Account, Observable<Pair<Account, Token>>>() {
                    @Override
                    public Observable<Pair<Account, Token>> call(final Account account) {
                        return mTokenRepository.getAccessToken(email, password)
                                .map(new Func1<Token, Pair<Account, Token>>() {
                                    @Override
                                    public Pair<Account, Token> call(Token token) {
                                        return Pair.create(account, token);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<Account, Token>>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onShowProgress(false, false);
                        getView().onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        Timber.d(e.getMessage());

                        getView().onShowProgress(false, false);

                        final String message = e.getMessage();

                        if (message.contains("timeout") ||
                                message.contains("Unable to resolve host")) {
                            getView().onNetworkError();
                        } else if (message.contains("409")) {
                            if (message.contains("Email")) {
                                getView().onEmailExistsError(AuthProvider.PROVIDER_YOLOO);
                            } else {
                                getView().onUsernameExistsError();
                            }
                        } else if (message.contains("401")) {
                            getView().onUnauthorized();
                        } else if (message.contains("400")) {
                            Timber.e("Bad request: %s", message);
                        }
                    }

                    @Override
                    public void onNext(Pair<Account, Token> pair) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onSaveUser(pair.first);
                        getView().onSaveToken(pair.second);
                    }
                });
    }

    public void signIn(final String email, final String password) {
        if (!isViewAttached()) {
            return;
        }

        getView().onShowProgress(true, true);

        mTokenSubscription = mTokenRepository.getAccessToken(email, password)
                .flatMap(new Func1<Token, Observable<Pair<Account, Token>>>() {
                    @Override
                    public Observable<Pair<Account, Token>> call(final Token token) {
                        return mUserRepository.get(token.getAccessToken().toCharArray())
                                .map(new Func1<Account, Pair<Account, Token>>() {
                                    @Override
                                    public Pair<Account, Token> call(Account account) {
                                        return Pair.create(account, token);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<Account, Token>>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onShowProgress(false, true);
                        getView().onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        Timber.d(e.getMessage());

                        getView().onShowProgress(false, true);

                        final String message = e.getMessage();

                        if (message.contains("timeout") ||
                                message.contains("Unable to resolve host")) {
                            getView().onNetworkError();
                        } else if (message.contains("400")) {
                            if (message.contains("email")) {
                                getView().onInvalidEmail();
                            } else if (message.contains("password")) {
                                getView().onInvalidPassword();
                            }
                        }
                    }

                    @Override
                    public void onNext(Pair<Account, Token> pair) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onSaveToken(pair.second);
                        getView().onSaveUser(pair.first);
                    }
                });
    }

    public void sendGoogleToken(final String token, final String locale) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository.add(token, "google", locale)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        Timber.e("Error: %s", e.getMessage());

                        getView().onEmailExistsError(AuthProvider.PROVIDER_GOOGLE);
                    }

                    @Override
                    public void onNext(Account account) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onSaveUser(account);
                        getView().onSaveToken(new Token().setAccessToken(token));
                    }
                });
    }
}
