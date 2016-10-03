package com.backpackers.android.ui.follow;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAccount;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FollowPresenter extends BaseMvpPresenter<FollowView> {

    private final UserRepository mUserRepository;

    private Subscription mUserSubscription;

    public FollowPresenter(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mUserSubscription);
    }


    public void listFollowers(final char[] accessToken, final String userId,
                              final String nextPageToken, final int limit) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository
                .listFollowers(accessToken, userId, nextPageToken, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseAccount>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onError(e);
                    }

                    @Override
                    public void onNext(CollectionResponseAccount collection) {
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

    public void listFollowees(final char[] accessToken, final String userId,
                              final String nextPageToken, final int limit) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository
                .listFollowees(accessToken, userId, nextPageToken, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseAccount>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onError(e);
                    }

                    @Override
                    public void onNext(CollectionResponseAccount collection) {
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
}
