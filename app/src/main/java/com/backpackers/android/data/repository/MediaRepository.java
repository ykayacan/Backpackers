package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseMedia;
import com.backpackers.android.data.repository.remote.MediaService;

import rx.Observable;

public class MediaRepository {

    private final MediaService mService;

    public MediaRepository(MediaService service) {
        mService = service;
    }

    public Observable<CollectionResponseMedia> get(final char[] accessToken,
                                                   final String nextPageToken) {
        return mService.get(accessToken, nextPageToken);
    }

    public Observable<CollectionResponseMedia> get(final char[] accessToken, final String userId,
                                                   final String nextPageToken) {
        return mService.get(accessToken, userId, nextPageToken);
    }
}