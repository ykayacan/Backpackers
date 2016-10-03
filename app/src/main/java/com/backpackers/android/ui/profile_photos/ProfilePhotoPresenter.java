package com.backpackers.android.ui.profile_photos;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseMedia;
import com.backpackers.android.data.repository.MediaRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfilePhotoPresenter extends BaseMvpPresenter<ProfilePhotoView> {

    private final MediaRepository mMediaRepository;

    private Subscription mMediaSubscription;

    public ProfilePhotoPresenter(MediaRepository mediaRepository) {
        mMediaRepository = mediaRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mMediaSubscription);
    }

    public void listMedias(final boolean pullToRefresh,
                           final char[] accessToken,
                           final String nextPageToken) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mMediaSubscription = mMediaRepository.get(accessToken, nextPageToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseMedia>() {
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
                    public void onNext(CollectionResponseMedia collection) {
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

    public void listMedias(final boolean pullToRefresh,
                           final char[] accessToken,
                           final String userId,
                           final String nextPageToken) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mMediaSubscription = mMediaRepository.get(accessToken, userId, nextPageToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseMedia>() {
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
                    public void onNext(CollectionResponseMedia collection) {
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
