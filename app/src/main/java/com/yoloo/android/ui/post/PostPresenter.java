package com.yoloo.android.ui.post;

import com.yoloo.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.yoloo.android.data.repository.PostRepository;
import com.yoloo.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostPresenter extends BaseMvpPresenter<PostView> {

    private final PostRepository mRepository;
    private Subscription mSubscription;

    public PostPresenter(PostRepository mRepository) {
        this.mRepository = mRepository;
    }

    @Override
    public void onViewAttached(PostView view) {
        super.onViewAttached(view);
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    public void loadTimeline(final boolean pullToRefresh, final char[] accessToken) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mSubscription = mRepository.list(accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseAbstractPost>() {
                    @Override
                    public void onCompleted() {
                        getView().onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().onError(e, pullToRefresh);
                    }

                    @Override
                    public void onNext(CollectionResponseAbstractPost collectionResponseAbstractPost) {
                        getView().onDataArrived(collectionResponseAbstractPost);
                    }
                });
    }
}
