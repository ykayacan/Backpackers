package com.backpackers.android.data.repository;

import com.backpackers.android.data.repository.remote.VoteService;

import rx.Observable;

public class VoteRepository {

    private final VoteService mService;

    public VoteRepository(VoteService service) {
        mService = service;
    }

    public Observable<Void> vote(final char[] accessToken,
                                 final String postId,
                                 final int direction) {
        return mService.vote(accessToken, postId, direction);
    }
}
