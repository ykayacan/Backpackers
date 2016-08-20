package com.yoloo.android.data.repository;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.repository.remote.UserService;

import rx.Observable;

public class AccountRepository {

    private final UserService mService;

    public AccountRepository(UserService service) {
        mService = service;
    }

    public Observable<Account> add(final String username,
                                   final String email,
                                   final String password) {
        return mService.createYolooAccount(username, email, password);
    }

    public Observable<Account> add(final String token,
                                   final String provider) {
        if (provider.equals("google")) {
            return mService.createGoogleAccount(token);
        } else {
            return mService.createFacebookAccount(token);
        }
    }
}
