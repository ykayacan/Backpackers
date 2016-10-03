package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseNotification;
import com.backpackers.android.data.repository.remote.NotificationService;

import rx.Observable;

public class NotificationRepository {

    private final NotificationService mService;

    public NotificationRepository(NotificationService service) {
        mService = service;
    }

    public Observable<Void> register(final char[] accessToken, final String regId) {
        return mService.register(accessToken, regId);
    }

    public Observable<Void> unregister(final char[] accessToken, final String regId) {
        return mService.unregister(accessToken, regId);
    }

    public Observable<Void> add(final char[] accessToken,
                                final String receiverId,
                                final String action,
                                final String content,
                                final String postId,
                                final String commentId) {
        return mService.add(accessToken, receiverId, action, content, postId, commentId);
    }

    public Observable<Void> remove(final char[] accessToken,
                                   final String notificationId) {
        return mService.remove(accessToken, notificationId);
    }

    public Observable<CollectionResponseNotification> list(final char[] accessToken,
                                                           final String cursor) {
        return mService.list(accessToken, cursor);
    }
}
