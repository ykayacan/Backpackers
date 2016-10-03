package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class FollowService {

    public Observable<Void> follow(final char[] accessToken,
                                   final String userId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .follows()
                        .follow(userId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> unFollow(final char[] accessToken,
                                   final String userId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .follows()
                        .unFollow(userId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
