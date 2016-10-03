package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.backpackers.android.data.repository.remote.PostService;

import rx.Observable;

public class TimelineRepository {

    private final PostService mService;

    public TimelineRepository(PostService mService) {
        this.mService = mService;
    }

    public Observable<AbstractPost> add(final String content,
                                        final String hashtags,
                                        final String location,
                                        final String mediaIds) {
        return mService.add(content, hashtags, location, mediaIds);
    }

    public Observable<CollectionResponseAbstractPost> list(final char[] accessToken,
                                                           final String nextPageToken) {
        return mService.list(accessToken, nextPageToken);
    }
}
