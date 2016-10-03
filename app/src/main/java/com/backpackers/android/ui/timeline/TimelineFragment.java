package com.backpackers.android.ui.timeline;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.backpackers.android.data.repository.LikeRepository;
import com.backpackers.android.data.repository.TimelineRepository;
import com.backpackers.android.data.repository.remote.LikeService;
import com.backpackers.android.data.repository.remote.PostService;
import com.backpackers.android.ui.base.BaseAuthFragment;
import com.backpackers.android.ui.home.HomeActivity;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnLikeClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.VerticalSpaceItemDecoration;
import com.backpackers.android.ui.timeline.adapter.TimelineAdapter;
import com.backpackers.android.ui.timeline.adapter_delegates.TimelineImageDelegate;
import com.backpackers.android.ui.timeline.adapter_delegates.TimelineTextDelegate;
import com.backpackers.android.ui.timeline.adapter_delegates.TimelineVideoDelegate;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import butterknife.BindView;
import timber.log.Timber;

public class TimelineFragment extends BaseAuthFragment<TimelineView, TimelinePresenter> implements
        TimelineView, SwipeRefreshLayout.OnRefreshListener,
        OnLikeClickListener, OnCommentsClickListener, OnProfileClickListener,
        HomeActivity.OnTabReselectedListener,
        EndlessRecyclerViewScrollListener.OnLoadMoreListener {

    @BindView(R.id.list_timeline)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_timeline)
    SwipeRefreshLayout mRefreshLayout;

    private boolean mIsFirstLoad = true;

    private TimelineAdapter mAdapter;

    private String mNextPageToken = null;

    private boolean mIsRefresh = false;

    private Resources mRes;

    private EndlessRecyclerViewScrollListener mScrollListener;

    public TimelineFragment() {
        // Required empty public constructor
    }

    public static TimelineFragment newInstance() {
        return new TimelineFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeActivity) context).setOnTabReselectedListener(this);
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_post;
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
            mIsFirstLoad = true;
        }
    }

    @NonNull
    @Override
    public TimelinePresenter createPresenter() {
        return new TimelinePresenter(
                new TimelineRepository(new PostService()),
                new LikeRepository(new LikeService()));
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
        mAdapter.setShouldLoadMore(false);

        Timber.d("onEmpty()");
    }

    @Override
    public void onDataArrived(CollectionResponseAbstractPost data) {
        // Assign new cursor.
        mNextPageToken = data.getNextPageToken();

        if (mIsRefresh) {
            mAdapter.refreshData(data.getItems());
            mRecyclerView.scrollToPosition(0);
            mIsRefresh = false;
        } else {
            mAdapter.addAllToEnd(data.getItems());
        }

        mAdapter.setShouldLoadMore(false);

        Timber.d("onDataArrived()");
    }

    @Override
    public void onLoadFinished() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        // Reset values to initial states.
        mNextPageToken = null;
        mIsRefresh = true;
        mScrollListener.resetPageCount();

        onLoadStarted(true, null);
    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore()");

        mAdapter.setShouldLoadMore(true);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onReselect(TabLayout.Tab tab) {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        super.onSignedIn(account);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onCommentClick(View v, String postId, String postOwnerId) {

    }

    @Override
    public void onProfileClick(View v, String userId, String username) {
        ProfileActivity.startProfileActivity(getContext(), userId, username);
    }

    @Override
    public void onLiked(String postId) {
        getPresenter().like(getAccessToken(), postId);
    }

    @Override
    public void onUnLiked(String postId) {
        getPresenter().dislike(getAccessToken(), postId);
    }

    @Override
    public void onLikeSuccessful() {

    }

    @Override
    public void onLikeFailed() {

    }

    @Override
    public void onDislikeSuccessful() {

    }

    @Override
    public void onDislikeFailed() {

    }

    private void setupRecyclerView() {
        mAdapter = new TimelineAdapter();

        AdapterManager<AbstractPost> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new TimelineImageDelegate(this, this, this, mRes));
        manager.add(new TimelineTextDelegate(this, this, this, mRes));
        manager.add(new TimelineVideoDelegate(this, this, this, mRes));

        mAdapter.setAdapterManager(manager);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewOptions() {
        final LinearLayoutManager lm = new LinearLayoutManager(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lm);
        final SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(8));

        mScrollListener = new EndlessRecyclerViewScrollListener(mAdapter, lm, this);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }
}