package com.backpackers.android.ui.search;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAccount;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseHashTag;
import com.backpackers.android.data.repository.SearchRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SearchPresenter extends BaseMvpPresenter<SearchView> {

    private final UserRepository mUserRepository;
    private final SearchRepository mSearchRepository;

    private Subscription mUserSubscription;
    private Subscription mSearchSubscription;

    public SearchPresenter(UserRepository userRepository, SearchRepository searchRepository) {
        mUserRepository = userRepository;
        mSearchRepository = searchRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mUserSubscription);
    }

    public void searchUsers(final char[] accessToken, final String query,
                            final String nextPageToken, final int limit) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository.list(accessToken, query, nextPageToken, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseAccount>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
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

    public void searchHashTags(final char[] accessToken, final String query,
                               final String nextPageToken, final int limit) {
        if (!isViewAttached()) {
            return;
        }

        mSearchSubscription = mSearchRepository.searchHashTags(accessToken, query, nextPageToken, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseHashTag>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onNext(CollectionResponseHashTag collection) {
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
