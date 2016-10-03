package com.backpackers.android.data.repository;

import com.backpackers.android.data.repository.remote.FollowService;

import rx.Observable;

public class FollowRepository {

    private FollowService mService;

    public FollowRepository(FollowService service) {
        mService = service;
    }

    public Observable<Void> follow(final char[] accessToken, final String userId) {
        return mService.follow(accessToken, userId);
    }

    public Observable<Void> unFollow(final char[] accessToken, final String userId) {
        return mService.unFollow(accessToken, userId);
    }
}
