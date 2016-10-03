package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseComment;
import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class CommentService {

    public Observable<Comment> addForumComment(final char[] accessToken,
                                               final String postId,
                                               final String comment) {
        return Observable.fromCallable(new Callable<Comment>() {
            @Override
            public Comment call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .comments()
                        .add(postId, comment)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Comment> addTimelineComment(final char[] accessToken,
                                                  final String postId,
                                                  final String comment) {
        return Observable.fromCallable(new Callable<Comment>() {
            @Override
            public Comment call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .comments()
                        .add(postId, comment)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> removeForumComment(final char[] accessToken,
                                               final String postId,
                                               final String commentId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .comments()
                        .remove(postId, commentId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> removeTimelineComment(final char[] accessToken,
                                                  final String postId,
                                                  final String commentId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .posts()
                        .comments()
                        .remove(postId, commentId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseComment> list(final char[] accessToken,
                                                      final String postId,
                                                      final String nextPageToken) {
        return Observable.fromCallable(new Callable<CollectionResponseComment>() {
            @Override
            public CollectionResponseComment call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .comments()
                        .list(postId)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
