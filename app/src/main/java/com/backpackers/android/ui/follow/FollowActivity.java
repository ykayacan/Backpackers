package com.backpackers.android.ui.follow;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseAccount;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.ui.base.BaseAuthActivity;
import com.backpackers.android.ui.follow.adapter.FollowAdapter;
import com.backpackers.android.ui.follow.delegates.FollowDelegate;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.SlideInItemAnimator;
import com.backpackers.android.ui.recyclerview.VerticalSpaceItemDecoration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindString;
import butterknife.BindView;
import timber.log.Timber;

public class FollowActivity extends BaseAuthActivity<FollowView, FollowPresenter> implements
        FollowView, EndlessRecyclerViewScrollListener.OnLoadMoreListener, OnItemClickListener<Account> {

    public static final int MODE_FOLLOWERS = 0;
    public static final int MODE_FOLOWINGS = 1;

    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.list_follow)
    RecyclerView mRecyclerView;

    @BindString(R.string.title_activity_follow_followees)
    String mFollowingString;

    @BindString(R.string.title_activity_follow_followers)
    String mFollowersString;

    private FollowAdapter mAdapter = new FollowAdapter();

    private final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

    private String mNextPageToken = null;

    private int mMode;
    private String mUserId;

    private EndlessRecyclerViewScrollListener mScrollListener;

    public static void open(Context context, int mode, String userId) {
        Intent i = new Intent(context, FollowActivity.class);
        i.putExtra(EXTRA_MODE, mode);
        i.putExtra(EXTRA_USER_ID, userId);

        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        final Intent i = getIntent();

        mMode = i.getIntExtra(EXTRA_MODE, 0);
        mUserId = i.getStringExtra(EXTRA_USER_ID);

        setupToolbar();

        setupRecyclerView();
        setupRecyclerViewOptions();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    protected FollowPresenter createPresenter() {
        return new FollowPresenter(new UserRepository(new UserService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {
        Timber.d("Mode: %s", mMode);
        if (mMode == 0) {
            if (getUserId().equals(mUserId)) {
                getPresenter().listFollowers(getAccessToken(), getUserId(), mNextPageToken, 20);
            } else {
                getPresenter().listFollowers(getAccessToken(), mUserId, mNextPageToken, 20);
            }
        } else {
            if (getUserId().equals(mUserId)) {
                getPresenter().listFollowees(getAccessToken(), getUserId(), mNextPageToken, 20);
            } else {
                getPresenter().listFollowees(getAccessToken(), mUserId, mNextPageToken, 20);
            }
        }
    }

    @Override
    public void onLoading(boolean isPullToRefresh) {

    }

    @Override
    public void onError(Throwable e) {
        if (e.getMessage().contains("401")) {
            refreshToken();
        }
    }

    @Override
    public void onEmpty() {
        Timber.d("onEmpty()");
    }

    @Override
    public void onDataArrived(CollectionResponseAccount data) {
        // Assign new cursor.
        mNextPageToken = data.getNextPageToken();

        mAdapter.addAllToEnd(data.getItems());
    }

    @Override
    public void onLoadFinished() {

    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore()");

        //mAdapter.setShouldLoadMore(true);

        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        super.onSignedIn(account);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onItemClick(View v, Account account) {
        ProfileActivity.startProfileActivity(this, account.getId(), account.getUsername());
    }

    private void setupRecyclerView() {
        AdapterManager<Account> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new FollowDelegate(this));

        mAdapter.setAdapterManager(manager);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewOptions() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final SlideInItemAnimator animator = new SlideInItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(8));

        mScrollListener = new EndlessRecyclerViewScrollListener(mAdapter, mLayoutManager, this);

        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);

            if (mMode == MODE_FOLLOWERS) {
                ab.setTitle(mFollowersString);
            } else {
                ab.setTitle(mFollowingString);
            }
        }
    }
}
