package com.backpackers.android.ui.forum;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.data.repository.ForumRepository;
import com.backpackers.android.data.repository.SearchRepository;
import com.backpackers.android.data.repository.UploadRepository;
import com.backpackers.android.data.repository.VoteRepository;
import com.backpackers.android.data.repository.remote.ForumService;
import com.backpackers.android.data.repository.remote.SearchService;
import com.backpackers.android.data.repository.remote.UploadService;
import com.backpackers.android.data.repository.remote.VoteService;
import com.backpackers.android.internal.SimpleOnTabSelectedAdapter;
import com.backpackers.android.ui.base.BaseAuthFragment;
import com.backpackers.android.ui.comment.CommentActivity;
import com.backpackers.android.ui.forum.adapter.ForumAdapter;
import com.backpackers.android.ui.forum.adapter_delegates.ForumImageDelegate;
import com.backpackers.android.ui.forum.adapter_delegates.ForumTextDelegate;
import com.backpackers.android.ui.home.HomeActivity;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnContentImageClickListener;
import com.backpackers.android.ui.listeners.OnHashTagClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.listeners.OnReadMoreClickListener;
import com.backpackers.android.ui.listeners.OnSavePostClickListener;
import com.backpackers.android.ui.listeners.OnVoteActionClickListener;
import com.backpackers.android.ui.photo.PhotoActivity;
import com.backpackers.android.ui.post_detail.PostDetailActivity;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.SlideInItemAnimator;
import com.backpackers.android.ui.recyclerview.VerticalSpaceItemDecoration;
import com.backpackers.android.ui.search.SearchActivity;
import com.backpackers.android.ui.write.WriteActivity;
import com.backpackers.android.util.KeyboardUtils;
import com.backpackers.android.util.Utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import timber.log.Timber;

public class ForumFragment extends BaseAuthFragment<ForumView, ForumPresenter> implements
        ForumView, SwipeRefreshLayout.OnRefreshListener,
        OnVoteActionClickListener, OnCommentsClickListener, OnProfileClickListener,
        OnContentImageClickListener, OnSavePostClickListener,
        OnHashTagClickListener, OnReadMoreClickListener,
        HomeActivity.OnTabReselectedListener,
        EndlessRecyclerViewScrollListener.OnLoadMoreListener {

    public static final int DIR_UP = 1;
    public static final int DIR_DEFAULT = 0;
    public static final int DIR_DOWN = -1;

    public static final String BUNDLE_TARGET_USER_ID = "BUNDLE_TARGET_USER_ID";
    public static final String BUNDLE_HASHTAG = "BUNDLE_HASHTAG";

    private static final String SORT_NEWEST = "newest";
    private static final String SORT_HOT = "hot";

    private static final int NOTIFY_ID = 1;

    private final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.tablayout_category)
    TabLayout mTabLayout;

    @BindView(R.id.content_root)
    ViewGroup mRootView;

    @BindColor(R.color.background_lightish)
    int mBackgroundColor;

    private String mTargetUserId;
    private String mHashTag;

    private boolean mIsFirstLoad = true;

    private ForumAdapter mAdapter = new ForumAdapter();

    private String mNextPageToken = null;

    private boolean mIsRefresh = false;

    private boolean mIsBigLayout = false;

    private String mSort = SORT_NEWEST;

    private Resources mRes;

    private EndlessRecyclerViewScrollListener mScrollListener;

    private NotificationManager mNotificationManager;

    private NotificationCompat.Builder mNotificationBuilder;

    public ForumFragment() {
        // Required empty public constructor
    }

    public static ForumFragment newInstance(@Nullable String targetUserId,
                                            @Nullable String hashTag) {
        ForumFragment fragment = new ForumFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TARGET_USER_ID, targetUserId);
        bundle.putString(BUNDLE_HASHTAG, hashTag);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mTargetUserId = bundle.getString(BUNDLE_TARGET_USER_ID);
            mHashTag = bundle.getString(BUNDLE_HASHTAG);
        }

        setHasOptionsMenu(TextUtils.isEmpty(mTargetUserId));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setOnRefreshListener(this);

        mRes = getResources();

        if (TextUtils.isEmpty(mTargetUserId) && TextUtils.isEmpty(mHashTag)) {
            setupFab();
            setupTabs();

            ((HomeActivity) getActivity()).setOnTabReselectedListener(this);
        } else if (TextUtils.isEmpty(mTargetUserId) && !TextUtils.isEmpty(mHashTag)) {
            setSearchViewModeLayouts();
        } else {
            mTabLayout.setVisibility(View.GONE);
            mIsBigLayout = !mIsBigLayout;
            mRefreshLayout.setEnabled(false);
        }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WriteActivity.REQUEST_UPDATE_PAGE) {
            if (resultCode == HomeActivity.RESULT_OK) {
                processNewPost(data);
            }
        } else if (requestCode == PostDetailActivity.REQUEST_DETAIL) {
            if (resultCode == PostDetailActivity.RESULT_OK) {
                processPostDeletion(data);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forum, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_layout:
                mIsBigLayout = !mIsBigLayout;
                setupRecyclerView();
                mScrollListener.resetPageCount();
                break;
            case android.R.id.home:
                removeSearchModeLayouts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_forum;
    }

    @Override
    public ForumPresenter createPresenter() {
        return new ForumPresenter(
                new ForumRepository(new ForumService()),
                new UploadRepository(new UploadService()),
                new VoteRepository(new VoteService()),
                new SearchRepository(new SearchService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {
        //Timber.d("Token: %s", String.valueOf(getAccessToken()));
        if (!TextUtils.isEmpty(mHashTag)) {
            getPresenter().searchPosts(getAccessToken(), mHashTag, mNextPageToken, 20);
        } else {
            switch (mSort) {
                case SORT_NEWEST:
                    getPresenter().list(isPullToRefresh, getAccessToken(), mTargetUserId, SORT_NEWEST, cursor);
                    break;
                case SORT_HOT:
                    getPresenter().list(isPullToRefresh, getAccessToken(), mTargetUserId, SORT_HOT, cursor);
                    break;
                default:
                    getPresenter().list(isPullToRefresh, getAccessToken(), mTargetUserId, SORT_NEWEST, cursor);
                    break;
            }
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
        } else if (e.getMessage().contains("timeout")) {
            onLoadStarted(false, mNextPageToken);
        }
    }

    @Override
    public void onEmpty() {
        //mAdapter.setShouldLoadMore(false);

        Timber.d("onEmpty()");
    }

    @Override
    public void onDataArrived(CollectionResponseForumPost data) {
        // Assign new cursor.
        mNextPageToken = data.getNextPageToken();

        Timber.d("onDataArrived()");

        if (mIsRefresh) {
            mAdapter.refreshData(data.getItems(), mRecyclerView);
            mIsRefresh = false;
        } else {
            mAdapter.addAllToEnd(data.getItems());
        }

        //mAdapter.setShouldLoadMore(false);
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

        onLoadStarted(true, mNextPageToken);
    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore()");

        //mAdapter.setShouldLoadMore(true);

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
    public void onHashTagClick(String hashTag) {
        SearchActivity.startSearchActivity(getActivity(), hashTag);
    }

    @Override
    public void onCommentClick(View v, String postId, String postOwnerId) {
        CommentActivity.start(getActivity(), v, postId, postOwnerId);
    }

    @Override
    public void onContentImageClick(View v, String url) {
        ActivityOptionsCompat options = null;
        if (Utils.hasL()) {
            options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), v, getString(R.string.transition_content_photo));
        }
        PhotoActivity.startPhotoActivity(getContext(), url, options);
    }

    @Override
    public void onProfileClick(View v, String userId, String username) {
        ProfileActivity.startProfileActivity(getContext(), userId, username);
    }

    @Override
    public void onVoteAction(String postId, int direction) {
        getPresenter().vote(getAccessToken(), postId, direction);
    }

    @Override
    public void onSaveClick(String postId) {
        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReadMoreClick(View v, int position, ForumPost post) {
        PostDetailActivity.start(this, post, position, PostDetailActivity.REQUEST_DETAIL);
    }

    @Override
    public void onPostSendingSuccessful(ForumPost post) {
        mAdapter.addToTop(post);
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onPostSendingFailed(Throwable e) {

    }

    @Override
    public void onShowUploadNotification() {
        mNotificationManager = (NotificationManager) getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_forum_arrow_up_24dp)
                .setContentTitle(getString(R.string.notification_title_media_upload))
                .setContentText(getString(R.string.notification_content_media_uploading))
                .setProgress(0, 0, true)
                .setOngoing(true);

        mNotificationManager.notify(NOTIFY_ID, mNotificationBuilder.build());
    }

    @Override
    public void onDismissUploadNotification() {
        mNotificationBuilder.setContentText(getString(R.string.notification_content_media_uploaded))
                .setOngoing(false)
                .setProgress(0, 0, false);

        mNotificationManager.notify(NOTIFY_ID, mNotificationBuilder.build());
        mNotificationManager.cancel(NOTIFY_ID);
    }

    @Override
    public void onVoteUpdate(ForumPost post) {
        mAdapter.update(post);
    }

    private void setupTabs() {
        final View newTabView = View.inflate(getContext(), R.layout.layout_custom_tab, null);

        final ImageView icon1 = (ImageView) newTabView.findViewById(R.id.image_tab);
        final TextView text1 = (TextView) newTabView.findViewById(R.id.text_tab);

        icon1.setImageResource(R.drawable.ic_google_trends__white_24dp);
        text1.setText(R.string.label_new);

        icon1.setSelected(true);
        text1.setSelected(true);

        final TabLayout.Tab tab1 = mTabLayout.newTab();
        tab1.setCustomView(newTabView);

        mTabLayout.addTab(tab1);

        final View hotTabView = View.inflate(getContext(), R.layout.layout_custom_tab, null);

        final ImageView icon2 = (ImageView) hotTabView.findViewById(R.id.image_tab);
        final TextView text2 = (TextView) hotTabView.findViewById(R.id.text_tab);

        icon2.setImageResource(R.drawable.ic_hot_white_24dp);
        text2.setText(R.string.label_hot);

        final TabLayout.Tab tab2 = mTabLayout.newTab();
        tab2.setCustomView(hotTabView);

        mTabLayout.addTab(tab2);

        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedAdapter() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mRefreshLayout.setRefreshing(true);
                // Reset values to initial states.
                mNextPageToken = null;
                mIsRefresh = true;
                mScrollListener.resetPageCount();

                if (tab.getPosition() == 0) {
                    mSort = SORT_NEWEST;
                } else if (tab.getPosition() == 1) {
                    mSort = SORT_HOT;
                }

                onLoadStarted(true, mNextPageToken);
            }
        });
    }

    private void setupRecyclerView() {
        AdapterManager<ForumPost> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new ForumTextDelegate(this, this, this, this, this, mRes, mIsBigLayout));
        manager.add(new ForumImageDelegate(this, this, this, this, this, this, mRes, mIsBigLayout));

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

    private void setupFab() {
        getActivity().findViewById(R.id.fab_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteActivity.startWriteActivity(ForumFragment.this, v, getUsername(),
                        getProfileImageUrl(), WriteActivity.REQUEST_UPDATE_PAGE);
            }
        });
    }

    private void processNewPost(Intent data) {
        final String content = data.getStringExtra(WriteActivity.EXTRA_TEXT_CONTENT);
        final ArrayList<String> mediaPaths = data.getStringArrayListExtra(WriteActivity.EXTRA_MEDIA_CONTENT);
        final String location = data.getStringExtra(WriteActivity.EXTRA_LOCATION);
        final String hashtags = data.getStringExtra(WriteActivity.EXTRA_HASHTAGS);
        final boolean isLocked = data.getBooleanExtra(WriteActivity.EXTRA_IS_LOCKED, false);
        final int awardRep = data.getIntExtra(WriteActivity.EXTRA_AWARD_REP, 0);

        getPresenter().add(getAccessToken(), getUserId(), content, hashtags, location,
                mediaPaths, isLocked, awardRep);
    }

    private void processPostDeletion(final Intent data) {
        mAdapter.remove(data.getIntExtra(PostDetailActivity.EXTRA_POST_POSITION, 0));
    }

    private void setSearchViewModeLayouts() {
        mTabLayout.setVisibility(View.GONE);
        mRefreshLayout.setEnabled(false);

        TextView searchTv = (TextView) getActivity().findViewById(R.id.text_search);
        searchTv.setVisibility(View.VISIBLE);
        searchTv.setText(getString(R.string.label_hashtag, mHashTag));

        EditText searchEdit = (EditText) getActivity().findViewById(R.id.edit_search);
        KeyboardUtils.hideKeyboard(getContext(), searchEdit);
        searchEdit.setVisibility(View.GONE);

        ImageButton clearBtn = (ImageButton) getActivity().findViewById(R.id.image_btn_clear);
        clearBtn.setVisibility(View.GONE);

        TabLayout searchTabLayout = (TabLayout) getActivity().findViewById(R.id.tablayout_main);
        searchTabLayout.setVisibility(View.GONE);

        ViewPager searchPager = (ViewPager) getActivity().findViewById(R.id.viewpager_search);
        searchPager.setVisibility(View.GONE);

        mRootView.setBackgroundColor(mBackgroundColor);
    }

    private void removeSearchModeLayouts() {
        getFragmentManager().beginTransaction()
                .remove(this)
                .commit();

        getActivity().findViewById(R.id.text_search).setVisibility(View.GONE);
        getActivity().findViewById(R.id.edit_search).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.viewpager_search).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.tablayout_main).setVisibility(View.VISIBLE);
    }
}
