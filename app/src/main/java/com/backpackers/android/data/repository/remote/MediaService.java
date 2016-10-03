package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseMedia;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class MediaService {

    public Observable<CollectionResponseMedia> get(final char[] accessToken,
                                                   final String nextPageToken) {
        return Observable.fromCallable(new Callable<CollectionResponseMedia>() {
            @Override
            public CollectionResponseMedia call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .me()
                        .medias()
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseMedia> get(final char[] accessToken, final String userId,
                                                   final String nextPageToken) {
        return Observable.fromCallable(new Callable<CollectionResponseMedia>() {
            @Override
            public CollectionResponseMedia call() throws Exception {
                return ServerHelper.getYolooApi()
                        .users()
                        .medias(userId)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
