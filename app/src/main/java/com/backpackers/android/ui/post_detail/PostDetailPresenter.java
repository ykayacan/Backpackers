package com.backpackers.android.ui.post_detail;

import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.data.repository.ForumRepository;
import com.backpackers.android.data.repository.LikeRepository;
import com.backpackers.android.data.repository.TimelineRepository;
import com.backpackers.android.data.repository.VoteRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostDetailPresenter extends BaseMvpPresenter<PostDetailView> {

    private ForumRepository mForumRepository;
    private TimelineRepository mTimelineRepository;
    private LikeRepository mLikeRepository;
    private VoteRepository mVoteRepository;

    private Subscription mForumSubscription;
    private Subscription mVoteSubscription;
    private Subscription mPostSubscription;
    private Subscription mLikeSubscription;

    public PostDetailPresenter(ForumRepository forumRepository,
                               VoteRepository voteRepository) {
        mForumRepository = forumRepository;
        mVoteRepository = voteRepository;
    }

    public PostDetailPresenter(TimelineRepository timelineRepository,
                               LikeRepository likeRepository) {
        mTimelineRepository = timelineRepository;
        mLikeRepository = likeRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mForumSubscription, mVoteSubscription, mPostSubscription, mLikeSubscription);
    }

    public void updateForumPost(final char[] accessToken, final String postId,
                                final String content, final String hashTags,
                                final String locations, final String mediaIds,
                                final boolean isLocked, final int awardRep,
                                final boolean isAccepted) {
        if (!isViewAttached()) {
            return;
        }

        mForumSubscription = mForumRepository.update(accessToken, postId, content, hashTags,
                locations, mediaIds, isLocked, awardRep, isAccepted)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ForumPost>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ForumPost post) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onForumPostUpdate(post);
                    }
                });
    }

    public void removeForumPost(final char[] accessToken, final String postId) {
        if (!isViewAttached()) {
            return;
        }

        mForumSubscription = mForumRepository.remove(accessToken, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void vote(final char[] accessToken, final String postId, final int direction) {
        if (!isViewAttached()) {
            return;
        }

        mVoteSubscription = mVoteRepository.vote(accessToken, postId, direction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
