package com.backpackers.android.ui.forum;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.data.model.upload.Media;
import com.backpackers.android.data.model.upload.MediaResponse;
import com.backpackers.android.data.repository.ForumRepository;
import com.backpackers.android.data.repository.SearchRepository;
import com.backpackers.android.data.repository.UploadRepository;
import com.backpackers.android.data.repository.VoteRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;
import com.backpackers.android.util.ParseUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ForumPresenter extends BaseMvpPresenter<ForumView> {

    private final ForumRepository mForumRepository;
    private final UploadRepository mUploadRepository;
    private final VoteRepository mVoteRepository;
    private final SearchRepository mSearchRepository;

    private Subscription mForumSubscription;
    private Subscription mVoteSubscription;
    private Subscription mUploadSubscription;
    private Subscription mSearchSubscription;

    public ForumPresenter(ForumRepository forumRepository,
                          UploadRepository uploadRepository,
                          VoteRepository voteRepository,
                          SearchRepository searchRepository) {
        mForumRepository = forumRepository;
        mUploadRepository = uploadRepository;
        mVoteRepository = voteRepository;
        mSearchRepository = searchRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mForumSubscription, mVoteSubscription,
                mUploadSubscription, mSearchSubscription);
    }

    public void add(final char[] accessToken, final String userId, final String content,
                    final String hashTags, final String location, final ArrayList<String> mediaPaths,
                    final boolean isLocked, final int awardRep) {
        if (!isViewAttached()) {
            return;
        }

        if (mediaPaths != null) {
            final List<File> files = new ArrayList<>(mediaPaths.size());
            for (String path : mediaPaths) {
                files.add(new File(path));
            }

            getView().onShowUploadNotification();

            mUploadSubscription = mUploadRepository.upload(userId, files)
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            getView().onDismissUploadNotification();
                            Timber.d(throwable);
                        }
                    })
                    .map(new Func1<Response, MediaResponse>() {
                        @Override
                        public MediaResponse call(Response response) {
                            try {
                                final String jsonData = response.body().string();
                                return ParseUtil.getGSON().fromJson(jsonData, MediaResponse.class);
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }
                        }
                    })
                    .map(new Func1<MediaResponse, String>() {
                        @Override
                        public String call(MediaResponse response) {
                            final StringBuilder builder = new StringBuilder();
                            for (Media media : response.getMedias()) {
                                builder.append(media.getId())
                                        .append(",");
                            }
                            // Remove last comma.
                            return builder.substring(0, builder.length() - 1);
                        }
                    })
                    .flatMap(new Func1<String, Observable<ForumPost>>() {
                        @Override
                        public Observable<ForumPost> call(String mediaIds) {
                            return mForumRepository.add(accessToken, content, hashTags,
                                    location, mediaIds, isLocked, awardRep);
                        }
                    })
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            getView().onDismissUploadNotification();
                            Timber.d(throwable);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ForumPost>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!isViewAttached()) {
                                return;
                            }

                            Timber.d("Error occured: %s", e.getMessage());
                            getView().onPostSendingFailed(e);
                            getView().onDismissUploadNotification();
                        }

                        @Override
                        public void onNext(ForumPost post) {
                            if (!isViewAttached()) {
                                return;
                            }

                            getView().onDismissUploadNotification();
                            getView().onPostSendingSuccessful(post);
                        }
                    });
        } else {
            mForumSubscription = mForumRepository.add(accessToken, content, hashTags, location, null,
                    isLocked, awardRep)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ForumPost>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!isViewAttached()) {
                                return;
                            }

                            Timber.d(e);

                            getView().onPostSendingFailed(e);
                        }

                        @Override
                        public void onNext(ForumPost post) {
                            if (!isViewAttached()) {
                                return;
                            }

                            getView().onPostSendingSuccessful(post);
                        }
                    });
        }
    }

    public void list(final boolean pullToRefresh, final char[] accessToken,
                     final String targetUserId, final String sort,
                     final String nextPageToken) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(pullToRefresh);

        mForumSubscription = mForumRepository.list(accessToken, targetUserId, sort, nextPageToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseForumPost>() {
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
                    public void onNext(CollectionResponseForumPost collection) {
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

    public void searchPosts(final char[] accessToken, final String query,
                            final String nextPageToken, final int limit) {
        if (!isViewAttached()) {
            return;
        }

        getView().onLoading(false);

        mSearchSubscription = mSearchRepository.searchPosts(accessToken, query, nextPageToken, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CollectionResponseForumPost>() {
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
                    public void onNext(CollectionResponseForumPost collection) {
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

    public void vote(final char[] accessToken, final String postId, final int direction) {
        if (!isViewAttached()) {
            return;
        }

        mVoteSubscription = mVoteRepository.vote(accessToken, postId, direction)
                .flatMap(new Func1<Void, Observable<ForumPost>>() {
                    @Override
                    public Observable<ForumPost> call(Void aVoid) {
                        return mForumRepository.get(accessToken, postId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ForumPost>() {
                    @Override
                    public void call(ForumPost post) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onVoteUpdate(post);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (!isViewAttached()) {
                            return;
                        }

                        Timber.d(throwable);
                    }
                });
    }
}
