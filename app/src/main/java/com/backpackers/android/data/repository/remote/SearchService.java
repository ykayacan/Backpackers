package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseHashTag;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class SearchService {

    public Observable<CollectionResponseHashTag> searchHashTags(final char[] accessToken,
                                                                final String query,
                                                                final String nextPageToken,
                                                                final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseHashTag>() {
            @Override
            public CollectionResponseHashTag call() throws Exception {
                return ServerHelper.getYolooApi()
                        .search()
                        .hashtags()
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

    public Observable<CollectionResponseForumPost> searchPosts(final char[] accessToken,
                                                               final String query,
                                                               final String nextPageToken,
                                                               final int limit) {
        return Observable.fromCallable(new Callable<CollectionResponseForumPost>() {
            @Override
            public CollectionResponseForumPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .search()
                        .posts()
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
