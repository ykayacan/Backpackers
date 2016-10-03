package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class LikeService {

    public Observable<Object> likePost(final char[] accessToken,
                                       final String postId) {
        return Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .like(postId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Object> unLikePost(final char[] accessToken,
                                         final String postId) {
        return Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .dislike(postId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Object> likeComment(final char[] accessToken,
                                          final String commnetId) {
        return Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return ServerHelper.getYolooApi()
                        .comments()
                        .like(commnetId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Object> unLikeComment(final char[] accessToken,
                                            final String postId) {
        return Observable.fromCallable(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return ServerHelper.getYolooApi()
                        .comments()
                        .dislike(postId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
