package com.backpackers.android.ui.timeline;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.backpackers.android.data.repository.LikeRepository;
import com.backpackers.android.data.repository.TimelineRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TimelinePresenter extends BaseMvpPresenter<TimelineView> {

    private final TimelineRepository mTimelineRepository;
    private final LikeRepository mLikeRepository;

    private Subscription mPostSubscription;
    private Subscription mLikeSubscription;

    public TimelinePresenter(TimelineRepository timelineRepository,
                             LikeRepository likeRepository) {
        mTimelineRepository = timelineRepository;
        mLikeRepository = likeRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (mPostSubscription != null && !mPostSubscription.isUnsubscribed()) {
            mPostSubscription.unsubscribe();
        }
        if (mLikeSubscription != null && !mLikeSubscription.isUnsubscribed()) {
            mLikeSubscription.unsubscribe();
        }
    }

    public void list(final boolean pullToRefresh,
                     final char[] accessToken,
                     final String nextPageToken) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mPostSubscription = mTimelineRepository.list(accessToken, nextPageToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseAbstractPost>() {
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
                    public void onNext(CollectionResponseAbstractPost collection) {
                        if (!isViewAttached()) {
                            return;
                        }

                        if (collection.getItems() == null) {
                            getView().onEmpty();
                        } else {
                            getView().onDataArrived(collection);
                        }
                    }
                });
    }

    public void like(final char[] accessToken, final String postId) {
        if (!isViewAttached()) {
            return;
        }

        mLikeSubscription = mLikeRepository.likePost(accessToken, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onLikeSuccessful();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onLikeFailed();
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    public void dislike(final char[] accessToken, final String postId) {
        if (!isViewAttached()) {
            return;
        }

        mLikeSubscription = mLikeRepository.unLikePost(accessToken, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onDislikeSuccessful();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onDislikeFailed();
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }
}
