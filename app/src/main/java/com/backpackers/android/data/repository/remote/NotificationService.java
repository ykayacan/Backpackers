package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseNotification;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class NotificationService {

    public Observable<Void> register(final char[] accessToken,
                                     final String regId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .registrations()
                        .register(regId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> unregister(final char[] accessToken,
                                       final String regId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .registrations()
                        .unregister(regId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> add(final char[] accessToken,
                                final String receiverId,
                                final String action,
                                final String content,
                                final String postId,
                                final String commentId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .notifications()
                        .add(action, receiverId)
                        .setCommentId(commentId)
                        .setPostId(postId)
                        .setContent(content)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> remove(final char[] accessToken,
                                   final String notificationId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .notifications()
                        .remove(notificationId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseNotification> list(final char[] accessToken,
                                                           final String cursor) {
        return Observable.fromCallable(new Callable<CollectionResponseNotification>() {
            @Override
            public CollectionResponseNotification call() throws Exception {
                return ServerHelper.getYolooApi()
                        .notifications()
                        .list()
                        .setCursor(cursor)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
