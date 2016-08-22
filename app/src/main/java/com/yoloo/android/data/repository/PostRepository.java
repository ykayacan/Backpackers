package com.yoloo.android.data.repository;

import com.yoloo.android.backend.modal.yolooApi.model.AbstractPost;
import com.yoloo.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.yoloo.android.data.repository.remote.PostService;

import rx.Observable;

public class PostRepository {

    private final PostService mService;

    public PostRepository(PostService mService) {
        this.mService = mService;
    }

    public Observable<AbstractPost> add(final String content,
                                        final String hashtags,
                                        final String location,
                                        final String mediaIds) {
        return mService.add(content, hashtags, location, mediaIds);
    }

    public Observable<CollectionResponseAbstractPost> list(final char[] accessToken) {
        return mService.list(accessToken);
    }
}
