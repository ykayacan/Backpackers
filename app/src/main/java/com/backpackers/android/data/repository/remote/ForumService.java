package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class ForumService {

    public Observable<ForumPost> get(final char[] accessToken, final String postId) {
        return Observable.fromCallable(new Callable<ForumPost>() {
            @Override
            public ForumPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .get(postId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<ForumPost> add(final char[] accessToken, final String content,
                                     final String hashTags, final String locations,
                                     final String mediaIds, final boolean isLocked,
                                     final int awardRep) {
        return Observable.fromCallable(new Callable<ForumPost>() {
            @Override
            public ForumPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .add(hashTags)
                        .setContent(content)
                        .setLocations(locations)
                        .setMediaIds(mediaIds)
                        .setIsLocked(isLocked)
                        .setAwardRep(awardRep)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<ForumPost> update(final char[] accessToken, final String postId,
                                        final String content, final String hashTags,
                                        final String locations, final String mediaIds,
                                        final boolean isLocked, final int awardRep,
                                        final boolean isAccepted) {
        return Observable.fromCallable(new Callable<ForumPost>() {
            @Override
            public ForumPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .update(postId)
                        .setContent(content)
                        .setHashtags(hashTags)
                        .setLocation(locations)
                        .setMediaIds(mediaIds)
                        .setIsLocked(isLocked)
                        .setAwardRep(awardRep)
                        .setIsAccepted(isAccepted)
                        .setMediaIds(mediaIds)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<Void> remove(final char[] accessToken,
                                   final String postId) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .remove(postId)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }

    public Observable<CollectionResponseForumPost> list(final char[] accessToken,
                                                        final String targetUserId,
                                                        final String sort,
                                                        final String nextPageToken) {
        return Observable.fromCallable(new Callable<CollectionResponseForumPost>() {
            @Override
            public CollectionResponseForumPost call() throws Exception {
                return ServerHelper.getYolooApi()
                        .questions()
                        .list()
                        .setSort(sort)
                        .setTargetUserId(targetUserId)
                        .setCursor(nextPageToken)
                        .setRequestHeaders(
                                new HttpHeaders()
                                        .setAuthorization("Bearer " + String.valueOf(accessToken)))
                        .execute();
            }
        });
    }
}
