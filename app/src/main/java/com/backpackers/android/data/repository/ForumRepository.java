package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.data.repository.remote.ForumService;

import rx.Observable;

public class ForumRepository {

    private final ForumService mService;

    public ForumRepository(ForumService service) {
        mService = service;
    }

    public Observable<ForumPost> get(final char[] accessToken, final String postId) {
        return mService.get(accessToken, postId);
    }

    public Observable<ForumPost> add(final char[] accessToken, final String content,
                                     final String hashTags, final String locations,
                                     final String mediaIds, final boolean isLocked,
                                     final int awardRep) {
        return mService.add(accessToken, content, hashTags, locations, mediaIds, isLocked, awardRep);
    }

    public Observable<ForumPost> update(final char[] accessToken, final String postId,
                                        final String content, final String hashTags,
                                        final String locations, final String mediaIds,
                                        final boolean isLocked, final int awardRep,
                                        final boolean isAccepted) {
        return mService.update(accessToken, postId, content, hashTags, locations, mediaIds, isLocked,
                awardRep, isAccepted);
    }

    public Observable<Void> remove(final char[] accessToken,
                                   final String postId) {
        return mService.remove(accessToken, postId);
    }

    public Observable<CollectionResponseForumPost> list(final char[] accessToken,
                                                        final String targetUserId,
                                                        final String sort,
                                                        final String nextPageToken) {
        return mService.list(accessToken, targetUserId, sort, nextPageToken);
    }
}
