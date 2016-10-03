package com.backpackers.android.ui.notification;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseNotification;
import com.backpackers.android.backend.modal.yolooApi.model.Notification;
import com.backpackers.android.data.repository.FollowRepository;
import com.backpackers.android.data.repository.NotificationRepository;
import com.backpackers.android.data.repository.remote.FollowService;
import com.backpackers.android.data.repository.remote.NotificationService;
import com.backpackers.android.ui.base.BaseAuthFragment;
import com.backpackers.android.ui.comment.CommentActivity;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.notification.adapter.NotificationAdapter;
import com.backpackers.android.ui.notification.adapter_delegates.NotificationDelegate;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.SimpleDividerItemDecoration;
import com.backpackers.android.ui.recyclerview.SlideInItemAnimator;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import timber.log.Timber;

public class NotificationFragment extends
        BaseAuthFragment<NotificationView, NotificationPresenter> implements
        NotificationView, EndlessRecyclerViewScrollListener.OnLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener, OnProfileClickListener, OnFollowBackListener, OnCommentsClickListener {

    @BindView(com.backpackers.android.R.id.list_notification)
    RecyclerView mRecyclerView;

    @BindView(com.backpackers.android.R.id.swipe_notification)
    SwipeRefreshLayout mRefreshLayout;

    @BindView(com.backpackers.android.R.id.root_main)
    ViewGroup mRootView;

    private NotificationAdapter mAdapter = new NotificationAdapter();

    private boolean mIsFirstLoad = true;

    private String mNextPageToken = null;

    private boolean mIsRefresh = false;

    private Resources mRes;

    private EndlessRecyclerViewScrollListener mScrollListener;

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setOnRefreshListener(this);

        mRes = getResources();

        setupRecyclerView();
        setupRecyclerViewOptions();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsFirstLoad) {
            onLoadStarted(false, mNextPageToken);
            mIsFirstLoad = false;
        }
    }

    @Override
    protected int getFragmentLayoutResId() {
        return com.backpackers.android.R.layout.fragment_notification;
    }

    @Override
    protected NotificationPresenter createPresenter() {
        return new NotificationPresenter(
                new NotificationRepository(new NotificationService()),
                new FollowRepository(new FollowService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {
        getPresenter().list(isPullToRefresh, getAccessToken(), cursor);
    }

    @Override
    public void onLoading(boolean isPullToRefresh) {
        mRefreshLayout.setRefreshing(isPullToRefresh);
    }

    @Override
    public void onError(Throwable e) {
        Timber.d("onError: %s", e.getMessage());

        mRefreshLayout.setRefreshing(false);

        if (e.getMessage().contains("401")) {
            refreshToken();
        }
    }

    @Override
    public void onEmpty() {
        Timber.d("onEmpty()");
    }

    @Override
    public void onDataArrived(CollectionResponseNotification data) {
        // Assign new cursor.
        mNextPageToken = data.getNextPageToken();

        if (mIsRefresh) {
            mAdapter.refreshData(data.getItems());
            mRecyclerView.smoothScrollToPosition(0);
            mIsRefresh = false;
        } else {
            mAdapter.addAllToEnd(data.getItems());
        }

        Timber.d("onDataArrived()");
    }

    @Override
    public void onLoadFinished() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore()");

        //mAdapter.setShouldLoadMore(true);

        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onRefresh() {
        // Reset values to initial states.
        mNextPageToken = null;
        mIsRefresh = true;
        mScrollListener.resetPageCount();

        onLoadStarted(true, mNextPageToken);
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        super.onSignedIn(account);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onProfileClick(View v, String userId, String username) {
        Timber.d("Sender id: %s", userId);
        ProfileActivity.startProfileActivity(getContext(), userId, username);
    }

    @Override
    public void onFollowedBack() {
        Snackbar.make(mRootView, com.backpackers.android.R.string.label_followed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onAlreadyFollowedError() {
        Snackbar.make(mRootView, com.backpackers.android.R.string.error_user_already_following, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onFollowBack(String userId) {
        getPresenter().follow(getAccessToken(), userId);
    }

    @Override
    public void onCommentClick(View v, String postId, String postOwnerId) {
        CommentActivity.start(getActivity(), v, postId, postOwnerId);
    }

    private void setupRecyclerView() {
        AdapterManager<Notification> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new NotificationDelegate(mRes, this, this, this));

        mAdapter.setAdapterManager(manager);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewOptions() {
        final LinearLayoutManager lm = new LinearLayoutManager(getContext());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lm);

        final SlideInItemAnimator animator = new SlideInItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        mScrollListener = new EndlessRecyclerViewScrollListener(mAdapter, lm, this);

        mRecyclerView.addOnScrollListener(mScrollListener);
    }
}
