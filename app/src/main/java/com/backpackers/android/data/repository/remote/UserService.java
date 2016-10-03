package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAccount;
import com.backpackers.android.util.AuthUtils;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;
import timber.log.Timber;

public class UserService {

    public Observable<Account> createYolooAccount(final String username,
                                                  final String email,
                                                  final String password,
                                                  final String locale) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .register()
                        .yoloo(AuthUtils.getEncodedValue(username, email, password), locale)
                        .execute();
            }
        });
    }

    public Observable<Account> createGoogleAccount(final String token, final String locale) {
        Timber.d("token: %s", token);
        Timber.d("locale: %s", locale);
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .register()
                        .google(token, locale)
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

    public Observable<Account> get(final char[] accessToken) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .me()
                        .get()
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Account> get(final char[] accessToken, final String userId) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .get(userId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Account> update(final char[] accessToken, final String mediaId) {
        return Observable.fromCallable(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .me()
                        .update()
                        .setMediaId(mediaId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAccount> listSelfFollowers(final char[] accessToken,
                                                                   final String nextPageToken, final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseAccount>() {
            @Override
            public CollectionResponseAccount call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .me()
                        .followers()
                        .setLimit(limit)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAccount> listSelfFollowees(final char[] accessToken,
                                                                   final String nextPageToken, final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseAccount>() {
            @Override
            public CollectionResponseAccount call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .me()
                        .followees()
                        .setLimit(limit)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAccount> listFollowers(final char[] accessToken, final String userId,
                                                               final String nextPageToken, final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseAccount>() {
            @Override
            public CollectionResponseAccount call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .followers(userId)
                        .setLimit(limit)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAccount> listFollowees(final char[] accessToken, final String userId,
                                                               final String nextPageToken, final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseAccount>() {
            @Override
            public CollectionResponseAccount call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .followees(userId)
                        .setLimit(limit)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAccount> list(final char[] accessToken, final String query,
                                                      final String nextPageToken, final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseAccount>() {
            @Override
            public CollectionResponseAccount call() throws Exception {
                return ServerHelper.getYolooApi()
                        .search()
                        .users()
                        .list(query)
                        .setLimit(limit)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
