package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class PostService {

    public Observable<AbstractPost> add(final String content, final String hashtags,
                                        final String location, final String mediaIds) {
        return Observable.fromCallable(new Callable<AbstractPost>() {
            @Override
            public AbstractPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .add(content, hashtags)
                        .setLocation(location)
                        .setMediaIds(mediaIds)
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAbstractPost> list(final char[] accessToken,
                                                           final String nextPageToken) {
        return Observable.fromCallable(new Callable<CollectionResponseAbstractPost>() {
            @Override
            public CollectionResponseAbstractPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .list()
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
