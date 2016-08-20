package com.yoloo.android.data.repository.remote;

import android.util.Base64;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class UserService {

    private static String getEncodedValue(String username, String email, String password) {
        final String credentials = username + ":" + password + ":" + email;
        return Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
    }

    public Observable<Account> createYolooAccount(final String username,
                                                  final String email,
                                                  final String password) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .register()
                        .yoloo(getEncodedValue(username, email, password))
                        .execute();
            }
        });
    }

    public Observable<Account> createGoogleAccount(final String token) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .register()
                        .google(token)
                        .execute();
            }
        });
    }

    public Observable<Account> createFacebookAccount(final String token) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .register()
                        .facebook(token)
                        .execute();
            }
        });
    }
}
