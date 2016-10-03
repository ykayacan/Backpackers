package com.backpackers.android.ui.profile;

import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.data.model.upload.MediaResponse;
import com.backpackers.android.data.repository.FollowRepository;
import com.backpackers.android.data.repository.UploadRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.framework.base.BaseMvpPresenter;
import com.backpackers.android.util.ParseUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ProfilePresenter extends BaseMvpPresenter<ProfileView> {

    private final UserRepository mUserRepository;
    private final FollowRepository mFollowRepository;
    private final UploadRepository mUploadRepository;

    private Subscription mUserSubscription;
    private Subscription mFollowSubscription;
    private Subscription mUploadSubscription;

    public ProfilePresenter(UserRepository userRepository,
                            FollowRepository followRepository,
                            UploadRepository uploadRepository) {
        mUserRepository = userRepository;
        mFollowRepository = followRepository;
        mUploadRepository = uploadRepository;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        unSubscribe(mUserSubscription, mFollowSubscription, mUploadSubscription);
    }

    public void getUserDetail(final char[] accessToken) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository.get(accessToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onNext(Account account) {
                        getView().onProfile(account);
                    }
                });
    }

    public void getUserDetail(final char[] accessToken, final String userId) {
        if (!isViewAttached()) {
            return;
        }

        mUserSubscription = mUserRepository.get(accessToken, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onNext(Account account) {
                        getView().onProfile(account);
                    }
                });
    }

    public void updateUser(final char[] accessToken, final String userId, String imagePath) {
        File file = new File(imagePath);

        mUploadSubscription = mUploadRepository.upload(userId, Collections.singletonList(file))
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
                .flatMap(new Func1<MediaResponse, Observable<Account>>() {
                    @Override
                    public Observable<Account> call(MediaResponse mediaResponse) {
                        return mUserRepository.update(accessToken, mediaResponse.getMedias().get(0).getId());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Account>() {
                    @Override
                    public void onCompleted() {
                        if (!isViewAttached()) {
                            return;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!isViewAttached()) {
                            return;
                        }

                        Timber.d("Error occured: %s", e.getMessage());
                    }

                    @Override
                    public void onNext(Account account) {
                        if (!isViewAttached()) {
                            return;
                        }

                        getView().onProfile(account);
                    }
                });
    }

    public void follow(final char[] accessToken, final String targetUserId) {
        if (!isViewAttached()) {
            return;
        }

        getView().onUserFollowed();

        mFollowSubscription = mFollowRepository.follow(accessToken, targetUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void unFollow(final char[] accessToken, final String userId) {
        if (!isViewAttached()) {
            return;
        }

        getView().onUserUnFollowed();

        mFollowSubscription = mFollowRepository.unFollow(accessToken, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }
}
