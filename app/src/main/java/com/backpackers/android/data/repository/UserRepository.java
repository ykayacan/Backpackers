package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAccount;
import com.backpackers.android.data.repository.remote.UserService;

import rx.Observable;

public class UserRepository {

    private final UserService mService;

    public UserRepository(UserService service) {
        mService = service;
    }

    public Observable<Account> add(final String username,
                                   final String email,
                                   final String password,
                                   final String locale) {
        return mService.createYolooAccount(username, email, password, locale);
    }

    public Observable<Account> add(final String token,
                                   final String provider,
                                   final String locale) {
        if (provider.equals("google")) {
            return mService.createGoogleAccount(token, locale);
        } else {
            return mService.createFacebookAccount(token);
        }
    }

    public Observable<Account> get(final char[] accessToken) {
        return mService.get(accessToken);
    }

    public Observable<Account> get(final char[] accessToken, final String userId) {
        return mService.get(accessToken, userId);
    }

    public Observable<Account> update(final char[] accessToken, final String mediaId) {
        return mService.update(accessToken, mediaId);
    }

    public Observable<CollectionResponseAccount> list(final char[] accessToken, final String query,
                                                      final String nextPageToken, final int limit) {
        return mService.list(accessToken, query, nextPageToken, limit);
    }

    public Observable<CollectionResponseAccount> listFollowers(final char[] accessToken, final String userId,
                                                               final String nextPageToken, final int limit) {
        if (userId == null) {
            return mService.listSelfFollowers(accessToken, nextPageToken, limit);
        } else {
            return mService.listFollowers(accessToken, userId, nextPageToken, limit);
        }
    }

    public Observable<CollectionResponseAccount> listFollowees(final char[] accessToken, final String userId,
                                                               final String nextPageToken, final int limit) {
        if (userId == null) {
            return mService.listSelfFollowings(accessToken, nextPageToken, limit);
        } else {
            return mService.listFollowings(accessToken, userId, nextPageToken, limit);
        }
    }
}
