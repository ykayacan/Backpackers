package com.yoloo.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.backend.modal.yolooApi.model.AbstractPost;
import com.yoloo.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.yoloo.android.util.ServerHelper;

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
                        .add(content, hashtags, location)
                        .setMediaIds(mediaIds)
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseAbstractPost> list(final char[] accessToken) {
        return Observable.fromCallable(new Callable<CollectionResponseAbstractPost>() {
            @Override
            public CollectionResponseAbstractPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .list()
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
