package com.backpackers.android.ui.profile_photos;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseMedia;
import com.backpackers.android.backend.modal.yolooApi.model.Media;
import com.backpackers.android.data.repository.MediaRepository;
import com.backpackers.android.data.repository.remote.MediaService;
import com.backpackers.android.ui.base.BaseAuthFragment;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.photo.PhotoActivity;
import com.backpackers.android.ui.profile_photos.adapter.ProfilePhotoAdapter;
import com.backpackers.android.ui.profile_photos.adapter_delegates.ProfilePhotoDelegate;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.GridInsetDecoration;
import com.backpackers.android.util.Utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import butterknife.BindView;
import timber.log.Timber;

public class ProfilePhotoFragment extends BaseAuthFragment<ProfilePhotoView, ProfilePhotoPresenter> implements
        SwipeRefreshLayout.OnRefreshListener,
        ProfilePhotoView, OnItemClickListener<Media>,
        EndlessRecyclerViewScrollListener.OnLoadMoreListener {

    private static final String BUNDLE_USER_ID = "BUNDLE_USER_ID";

    @BindView(R.id.list_profile_photo)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mRefreshLayout;

    private boolean mIsFirstLoad = true;

    private ProfilePhotoAdapter mAdapter = new ProfilePhotoAdapter();

    private EndlessRecyclerViewScrollListener mScrollListener;

    private String mNextPageToken = null;

    private boolean mIsRefresh = false;

    private String mUserId;

    public ProfilePhotoFragment() {
        // Required empty public constructor
    }

    public static ProfilePhotoFragment newInstance(String userId) {
        final ProfilePhotoFragment fragment = new ProfilePhotoFragment();
        final Bundle args = new Bundle();
        args.putString(BUNDLE_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getString(BUNDLE_USER_ID);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setOnRefreshListener(this);

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

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_profile_photo;
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {
        if (getUserId().equals(mUserId)) {
            getPresenter().listMedias(isPullToRefresh, getAccessToken(), cursor);
        } else {
            getPresenter().listMedias(isPullToRefresh, getAccessToken(), mUserId, cursor);
        }
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
        mRefreshLayout.setRefreshing(false);
        //mAdapter.setShouldLoadMore(false);

        Timber.d("onEmpty()");
    }

    @Override
    public void onDataArrived(CollectionResponseMedia data) {
        // Assign new cursor.
        mNextPageToken = data.getNextPageToken();

        if (mIsRefresh) {
            mAdapter.refreshData(data.getItems());
            mIsRefresh = false;
        } else {
            mAdapter.addAllToTop(data.getItems());
        }

        //mAdapter.setShouldLoadMore(false);

        Timber.d("onDataArrived()");
    }

    @Override
    public void onLoadFinished() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected ProfilePhotoPresenter createPresenter() {
        return new ProfilePhotoPresenter(new MediaRepository(new MediaService()));
    }

    @Override
    public void onItemClick(View v, Media media) {
        ActivityOptionsCompat options = null;
        if (Utils.hasL()) {
            options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), v, getString(R.string.transition_content_photo));
        }
        PhotoActivity.startPhotoActivity(getContext(), media.getDetail().getStd().getUrl(), options);
    }

    @Override
    public void onLoadMore() {
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onRefresh() {
        // Reset values to initial states.
        mNextPageToken = null;
        mIsRefresh = true;
        mScrollListener.resetPageCount();

        onLoadStarted(true, null);
    }

    private void setupRecyclerView() {
        AdapterManager<Media> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new ProfilePhotoDelegate(this));

        mAdapter.setAdapterManager(manager);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewOptions() {
        final GridLayoutManager lm = new GridLayoutManager(getContext(), 3);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lm);

        final SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new GridInsetDecoration(getContext(), R.dimen.rv_grid_inset));

        mScrollListener = new EndlessRecyclerViewScrollListener(mAdapter, lm, this);

        mRecyclerView.addOnScrollListener(mScrollListener);
    }
}
