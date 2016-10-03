package com.backpackers.android.data.repository;

import com.backpackers.android.data.repository.remote.LikeService;

import rx.Observable;

public class LikeRepository {

    private final LikeService mService;

    public LikeRepository(LikeService service) {
        mService = service;
    }

    public Observable<Object> likePost(final char[] accessToken,
                                       final String postId) {
        return mService.likePost(accessToken, postId);
    }

    public Observable<Object> unLikePost(final char[] accessToken,
                                         final String postId) {
        return mService.unLikePost(accessToken, postId);
    }

    public Observable<Object> likeComment(final char[] accessToken,
                                          final String commentId) {
        return mService.likeComment(accessToken, commentId);
    }

    public Observable<Object> unLikeComment(final char[] accessToken,
                                            final String commentId) {
        return mService.unLikeComment(accessToken, commentId);
    }
}
