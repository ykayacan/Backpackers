package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseHashTag;
import com.backpackers.android.data.repository.remote.SearchService;

import rx.Observable;

public class SearchRepository {

    private final SearchService mService;

    public SearchRepository(SearchService service) {
        mService = service;
    }

    public Observable<CollectionResponseHashTag> searchHashTags(final char[] accessToken,
                                                                final String query,
                                                                final String nextPageToken,
                                                                final int limit) {
        return mService.searchHashTags(accessToken, query, nextPageToken, limit);
    }

    public Observable<CollectionResponseForumPost> searchPosts(final char[] accessToken,
                                                               final String query,
                                                               final String nextPageToken,
                                                               final int limit) {
        return mService.searchPosts(accessToken, query, nextPageToken, limit);
    }
}
