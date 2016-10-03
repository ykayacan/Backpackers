package com.backpackers.android.ui.comment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseComment;
import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.data.repository.CommentRepository;
import com.backpackers.android.data.repository.LikeRepository;
import com.backpackers.android.data.repository.NotificationRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.CommentService;
import com.backpackers.android.data.repository.remote.LikeService;
import com.backpackers.android.data.repository.remote.NotificationService;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.internal.WeakHandler;
import com.backpackers.android.ui.base.BaseAuthActivity;
import com.backpackers.android.ui.comment.adapter.CommentAdapter;
import com.backpackers.android.ui.comment.adapter_delegates.CommentDelegate;
import com.backpackers.android.ui.listeners.OnHashTagClickListener;
import com.backpackers.android.ui.listeners.OnLikeClickListener;
import com.backpackers.android.ui.listeners.OnOptionsClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.recyclerview.AdapterManager;
import com.backpackers.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.backpackers.android.ui.recyclerview.SlideInItemAnimator;
import com.backpackers.android.ui.user_autocomplete.AutoCompleteMentionAdapter;
import com.backpackers.android.ui.user_autocomplete.AutocompleteMentionItem;
import com.backpackers.android.ui.user_autocomplete.SpaceTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class CommentActivity extends BaseAuthActivity<CommentView, CommentPresenter> implements
        CommentView, SwipeRefreshLayout.OnRefreshListener, OnLikeClickListener,
        OnProfileClickListener, OnHashTagClickListener, OnOptionsClickListener,
        EndlessRecyclerViewScrollListener.OnLoadMoreListener {

    public static final String EXTRA_COMMENT_COUNT = "comment_count";
    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_POST_OWNER_ID = "EXTRA_POST_OWNER_ID";
    private static final String EXTRA_DRAWING_START_LOCATION = "drawing_start_location";

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe)
    SwipeRefreshLayout mRefreshLayout;

    @BindView(R.id.btn_send_comment)
    ImageButton mSendCommentBtn;

    @BindView(R.id.edit_comment)
    MultiAutoCompleteTextView mCommentEt;

    @BindView(R.id.text_comment_count)
    TextView mCommentCountTv;

    private AutoCompleteMentionAdapter mMentionAdapter;

    private CommentAdapter mAdapter;

    private String mNextPageToken = null;

    private boolean mIsRefresh = false;

    private String mPostId;
    private String mPostOwnerId;

    private Resources mRes;

    private EndlessRecyclerViewScrollListener mScrollListener;

    private WeakHandler mHandler = new WeakHandler();

    private Runnable mDropdownRunnable = new Runnable() {
        @Override
        public void run() {
            mCommentEt.showDropDown();
        }
    };

    public static void start(Activity activity, View v, String postId, String postOwnerId) {
        Intent intent = new Intent(activity, CommentActivity.class);

        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);

        intent.putExtra(EXTRA_POST_ID, postId);
        intent.putExtra(EXTRA_POST_OWNER_ID, postOwnerId);
        if (v.getTag() != null) {
            intent.putExtra(EXTRA_COMMENT_COUNT, (long) v.getTag());
        }
        intent.putExtra(EXTRA_DRAWING_START_LOCATION, startingLocation[1]);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.bottom_up, R.anim.stay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mRefreshLayout.setOnRefreshListener(this);

        mRes = getResources();

        setupRecyclerView();
        setupRecyclerViewOptions();

        Intent intent = getIntent();
        mPostId = intent.getStringExtra(EXTRA_POST_ID);
        mPostOwnerId = intent.getStringExtra(EXTRA_POST_OWNER_ID);
        final long commentCount = intent.getLongExtra(EXTRA_COMMENT_COUNT, 0L);

        mCommentCountTv.setText(mRes.getString(R.string.label_comments_count, commentCount));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onLoadStarted(false, mNextPageToken);

        setupMentionsAdapter();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.stay, R.anim.bottom_down);
    }

    @Override
    public CommentPresenter createPresenter() {
        return new CommentPresenter(
                new CommentRepository(new CommentService()),
                new LikeRepository(new LikeService()),
                new UserRepository(new UserService()),
                new NotificationRepository(new NotificationService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {
        getPresenter().list(isPullToRefresh, getAccessToken(), mPostId, cursor);
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
    public void onDataArrived(CollectionResponseComment data) {
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
        mCommentCountTv.setText(mRes.getString(R.string.label_comments_count, mAdapter.getDataItemCount()));

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
    public void onLiked(String itemId) {
        getPresenter().like(getAccessToken(), itemId);
    }

    @Override
    public void onUnLiked(String itemId) {
        getPresenter().dislike(getAccessToken(), itemId);
    }

    @Override
    public void onProfileClick(View v, String userId, String username) {
        ProfileActivity.startProfileActivity(this, userId, username);
    }

    @Override
    public void onHashTagClick(String hashTag) {
        Toast.makeText(this, hashTag, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionsClick(final View v, final String postId, final String id) {
        final PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater()
                .inflate(R.menu.menu_popup_comment, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        getPresenter().remove(getAccessToken(), postId, id);
                        mAdapter.remove((int) v.getTag());
                        mCommentCountTv.setText(mRes.getString(R.string.label_comments_count, mAdapter.getDataItemCount()));
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void onLoadMore() {
        Timber.d("onLoadMore()");

        mAdapter.setShouldLoadMore(true);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        super.onSignedIn(account);
        onLoadStarted(false, mNextPageToken);
    }

    @Override
    public void onCommentSuccessful(Comment comment) {
        mAdapter.addToEnd(comment);
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());

        mCommentCountTv.setText(mRes.getString(R.string.label_comments_count, mAdapter.getDataItemCount()));
    }

    @Override
    public void onMentionListArrived(List<AutocompleteMentionItem> items) {
        mMentionAdapter.setItems(items);
        mHandler.post(mDropdownRunnable);
    }

    @OnClick(R.id.btn_send_comment)
    void sendComment() {
        if (validateComment()) {
            final String text = mCommentEt.getText().toString();
            getPresenter().add(getAccessToken(), mPostId, mPostOwnerId, getUserId(), text);

            mCommentEt.setText(null);
        }
    }

    private void setupRecyclerView() {
        mAdapter = new CommentAdapter();

        AdapterManager<Comment> manager = new AdapterManager<>(mAdapter, getUserId());
        manager.add(new CommentDelegate(this, this, this, this, mRes));

        mAdapter.setAdapterManager(manager);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewOptions() {
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setHasFixedSize(true);

        final SlideInItemAnimator animator = new SlideInItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);

        mScrollListener = new EndlessRecyclerViewScrollListener(mAdapter, lm, this);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void setupMentionsAdapter() {
        mMentionAdapter = new AutoCompleteMentionAdapter(this, getPresenter(), getAccessToken());
        mCommentEt.setAdapter(mMentionAdapter);
        mCommentEt.setTokenizer(new SpaceTokenizer());
    }

    private boolean validateComment() {
        if (TextUtils.isEmpty(mCommentEt.getText())) {
            mSendCommentBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            return false;
        }

        return true;
    }
}
