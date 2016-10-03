package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseComment;
import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.data.repository.remote.CommentService;

import rx.Observable;

public class CommentRepository {

    private final CommentService mService;

    public CommentRepository(CommentService service) {
        mService = service;
    }

    public Observable<Comment> addForumComment(final char[] accessToken, final String postId,
                                               final String comment) {
        return mService.addForumComment(accessToken, postId, comment);
    }

    public Observable<Comment> addTimelineComment(final char[] accessToken, final String postId,
                                                  final String comment) {
        return mService.addTimelineComment(accessToken, postId, comment);
    }

    public Observable<Void> removeForumComment(final char[] accessToken, final String postId,
                                               final String commentId) {
        return mService.removeForumComment(accessToken, postId, commentId);
    }

    public Observable<Void> removeTimelineComment(final char[] accessToken, final String postId,
                                                  final String commentId) {
        return mService.removeTimelineComment(accessToken, postId, commentId);
    }

    public Observable<CollectionResponseComment> list(final char[] accessToken, final String postId,
                                                      final String nextPageToken) {
        return mService.list(accessToken, postId, nextPageToken);
    }
}
