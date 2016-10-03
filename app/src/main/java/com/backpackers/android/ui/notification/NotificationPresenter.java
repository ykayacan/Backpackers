package com.backpackers.android.ui.notification;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseNotification;
import com.backpackers.android.data.repository.FollowRepository;
import com.backpackers.android.data.repository.NotificationRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NotificationPresenter extends BaseMvpPresenter<NotificationView> {

    private final NotificationRepository mNotificationRepository;
    private final FollowRepository mFollowRepository;

    private Subscription mNotificationSubscription;
    private Subscription mFollowSubscription;

    public NotificationPresenter(NotificationRepository notificationRepository,
                                 FollowRepository followRepository) {
        mNotificationRepository = notificationRepository;
        mFollowRepository = followRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mNotificationSubscription, mFollowSubscription);
    }

    public void list(final boolean pullToRefresh, final char[] accessToken, final String cursor) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mNotificationSubscription = mNotificationRepository.list(accessToken, cursor)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseNotification>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onError(e);
                    }

                    @Override
                    public void onNext(CollectionResponseNotification collection) {
                        if (!isViewAttached()) {
                            return;
                        }

                        if (collection.getItems() == null || collection.isEmpty()) {
                            getView().onEmpty();
                        } else {
                            getView().onDataArrived(collection);
                        }
                    }
                });
    }

    public void remove(final char[] accessToken, final String notificationId) {
        mNotificationSubscription = mNotificationRepository.remove(accessToken, notificationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void follow(final char[] accessToken, final String targetUserId) {
        if (!isViewAttached()) {
            return;
        }

        mFollowSubscription = mFollowRepository.follow(accessToken, targetUserId)
                .flatMap(new Func1<Void, Observable<?>>() {
                    @Override
                    public Observable<?> call(Void aVoid) {
                        return mNotificationRepository
                                .add(accessToken, targetUserId, "FOLLOW", null, null, null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onAlreadyFollowedError();
                    }

                    @Override
                    public void onNext(Object o) {
                        getView().onFollowedBack();
                    }
                });
    }
}
