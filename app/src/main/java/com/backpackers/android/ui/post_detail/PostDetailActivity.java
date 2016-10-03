package com.backpackers.android.ui.post_detail;

import com.backpackers.android.BuildConfig;
import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.data.repository.ForumRepository;
import com.backpackers.android.data.repository.VoteRepository;
import com.backpackers.android.data.repository.remote.ForumService;
import com.backpackers.android.data.repository.remote.VoteService;
import com.backpackers.android.ui.base.BaseAuthActivity;
import com.backpackers.android.ui.comment.CommentActivity;
import com.backpackers.android.ui.forum.ForumFragment;
import com.backpackers.android.ui.photo.PhotoActivity;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.search.SearchActivity;
import com.backpackers.android.ui.widget.CheckableImageButton;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.PatternEditableBuilder;
import com.backpackers.android.ui.widget.RelativeTimeTextView;
import com.backpackers.android.util.DrawableHelper;
import com.backpackers.android.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;

public class PostDetailActivity extends BaseAuthActivity<PostDetailView, PostDetailPresenter> implements
        PostDetailView, AutoLinkOnClickListener {

    public static final int REQUEST_DETAIL = 2000;
    public static final int REQUEST_UPDATE = 3000;

    public static final String EXTRA_POST_POSITION = "extra_post_position";

    public static final String EXTRA_VOTE_STATUS = "EXTRA_VOTE_STATUS";
    public static final String EXTRA_VOTE_DOWN_COUNT = "EXTRA_VOTE_DOWN_COUNT";
    public static final String EXTRA_VOTE_UP_COUNT = "EXTRA_VOTE_UP_COUNT";

    private static final String EXTRA_POST_ID = "extra_post_id";
    private static final String EXTRA_POST_OWNER_ID = "extra_post_owner_id";
    private static final String EXTRA_POST_USERNAME = "extra_post_username";
    private static final String EXTRA_POST_LOCATION = "extra_post_location";
    private static final String EXTRA_POST_PROFILE_IMAGE_URL = "extra_post_profile_image_url";
    private static final String EXTRA_POST_TIME_PASSED = "extra_post_time_passed";
    private static final String EXTRA_POST_CONTENT_TEXT = "extra_post_content_text";
    private static final String EXTRA_POST_CONTENT_IMAGE_URL = "extra_post_content_image_url";
    private static final String EXTRA_POST_LIKE_COUNT = "extra_post_like_count";
    private static final String EXTRA_POST_VOTE_UP_COUNT = "extra_post_vote_up_count";
    private static final String EXTRA_POST_VOTE_DOWN_COUNT = "extra_post_vote_down_count";
    private static final String EXTRA_POST_IS_LIKED = "extra_post_is_liked";
    private static final String EXTRA_POST_VOTE_STATUS = "extra_post_vote_status";
    private static final String EXTRA_POST_HASHTAGS = "extra_post_hashtags";
    private static final String EXTRA_POST_IS_COMMENTED = "extra_post_is_commented";
    private static final String EXTRA_POST_COMMENT_COUNT = "extra_post_comment_count";

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.image_avatar)
    CircleImageView mAvatarCiv;

    @BindView(R.id.text_username)
    TextView mUsernameTv;

    @BindView(R.id.text_location)
    TextView mLocationTv;

    @BindView(R.id.text_time)
    RelativeTimeTextView mTimePassedTv;

    @BindView(R.id.text_content)
    TextView mContentTv;

    @BindView(R.id.image_content)
    ImageView mContentImage;

    @BindView(R.id.text_hashTags)
    AutoLinkTextView mHasTagsTv;

    @BindView(R.id.image_btn_vote_up)
    CheckableImageButton mUpBtn;

    @BindView(R.id.ts_ups)
    TextSwitcher mUpsCountTs;

    @BindView(R.id.image_btn_vote_down)
    @Nullable
    CheckableImageButton mDownBtn;

    @BindView(R.id.ts_downs)
    @Nullable
    TextSwitcher mDownsCountTs;

    @BindView(R.id.image_btn_comment)
    ImageButton mCommentBtn;

    @BindView(R.id.text_comments)
    TextView mCommentCountTv;

    @BindView(R.id.image_btn_options)
    ImageButton mOptionsImageBtn;

    @BindColor(R.color.colorAccent)
    int mAccentColor;

    private int mPostPosition;
    private String mPostId;
    private String mPostOwnerId;
    private String mPostUsername;
    private String mLocation;
    private String mPostProfileImageUrl;
    private long mPostTimePassed;
    private String mPostContentText;
    private String mPostContentImageUrl;
    private long mPostLikeCount = -1;
    private Long mPostVoteUpCount;
    private Long mPostVoteDownCount;
    private boolean mPostIsLiked;
    private String mPostVoteStatus;
    private List<String> mPostHashTags;
    private boolean mPostIsCommented;
    private Long mPostCommentCount;

    public static void start(Fragment fragment, ForumPost post, int position, int requestCode) {
        final Intent intent = new Intent(fragment.getContext(), PostDetailActivity.class);
        intent.putExtra(EXTRA_POST_POSITION, position);
        intent.putExtra(EXTRA_POST_ID, post.getId());
        intent.putExtra(EXTRA_POST_OWNER_ID, post.getOwnerId());
        intent.putExtra(EXTRA_POST_USERNAME, post.getUsername());
        intent.putExtra(EXTRA_POST_LOCATION, post.getLocation().getName());
        intent.putExtra(EXTRA_POST_PROFILE_IMAGE_URL, post.getProfileImageUrl());
        intent.putExtra(EXTRA_POST_TIME_PASSED, post.getCreatedAt().getValue());
        intent.putExtra(EXTRA_POST_CONTENT_TEXT, post.getContent());
        intent.putExtra(EXTRA_POST_CONTENT_IMAGE_URL, post.getMedias() == null
                ? null : post.getMedias().get(0).getDetail().getStd().getUrl());
        intent.putExtra(EXTRA_POST_VOTE_UP_COUNT, post.getUps());
        intent.putExtra(EXTRA_POST_VOTE_DOWN_COUNT, post.getDowns());
        intent.putExtra(EXTRA_POST_VOTE_STATUS, post.getStatus());
        intent.putStringArrayListExtra(EXTRA_POST_HASHTAGS, (ArrayList<String>) post.getHashtags());
        intent.putExtra(EXTRA_POST_IS_COMMENTED, post.getCommented());
        intent.putExtra(EXTRA_POST_COMMENT_COUNT, post.getComments());

        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        setupToolbar();

        setupExtras();

        setupUserInfoPart();
        setupContentPart();
        setHashTags();
        setUpAndDownVoteStatus();
        setupComments();
        setupActionPart();

        mOptionsImageBtn.setVisibility(getUserId().equals(mPostOwnerId) ? View.VISIBLE : View.GONE);

        setupVoteButtons();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public PostDetailPresenter createPresenter() {
        return new PostDetailPresenter(
                new ForumRepository(new ForumService()),
                new VoteRepository(new VoteService()));
    }

    @Override
    public void onForumPostUpdate(ForumPost post) {

    }

    @Override
    public void onTimelinePostUpdate(AbstractPost post) {

    }

    @Override
    public void onVoteUpdate(ForumPost post) {
        mUpsCountTs.setText(String.valueOf(post.getUps()));
        if (mDownsCountTs != null) {
            mDownsCountTs.setText(String.valueOf(post.getDowns()));
        }
    }

    @Override
    public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
        SearchActivity.startSearchActivity(this, matchedText);
    }

    @OnClick({R.id.text_username, R.id.image_avatar})
    void openProfile(View v) {
        ProfileActivity.startProfileActivity(this, mPostOwnerId, mPostUsername);
    }

    @OnClick(R.id.image_btn_comment)
    void openComments(View view) {
        view.setTag(mPostCommentCount);
        CommentActivity.start(this, view, mPostId, mPostOwnerId);
    }

    @OnClick(R.id.image_btn_options)
    void openOptions(View view) {
        final PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_popup_post, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        getPresenter().removeForumPost(getAccessToken(), mPostId);
                        final Intent i = new Intent();

                        i.putExtra(EXTRA_POST_POSITION, mPostPosition);

                        setResult(Activity.RESULT_OK, i);
                        finish();
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    @OnClick(R.id.image_content)
    void openContentPhoto(View v) {
        ActivityOptionsCompat options = null;
        if (Utils.hasL()) {
            options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, v, getString(R.string.transition_content_photo));
        }
        PhotoActivity.startPhotoActivity(this, mPostContentImageUrl, options);
    }

    private void setupExtras() {
        final Intent i = getIntent();

        if (i != null) {
            mPostPosition = i.getIntExtra(EXTRA_POST_POSITION, 0);
            mPostId = i.getStringExtra(EXTRA_POST_ID);
            mPostOwnerId = i.getStringExtra(EXTRA_POST_OWNER_ID);
            mPostUsername = i.getStringExtra(EXTRA_POST_USERNAME);
            mLocation = i.getStringExtra(EXTRA_POST_LOCATION);
            mPostProfileImageUrl = i.getStringExtra(EXTRA_POST_PROFILE_IMAGE_URL);
            mPostTimePassed = i.getLongExtra(EXTRA_POST_TIME_PASSED, 0);
            mPostContentText = i.getStringExtra(EXTRA_POST_CONTENT_TEXT);
            if (i.hasExtra(EXTRA_POST_CONTENT_IMAGE_URL)) {
                mPostContentImageUrl = i.getStringExtra(EXTRA_POST_CONTENT_IMAGE_URL);
            }
            if (i.hasExtra(EXTRA_POST_LIKE_COUNT)) {
                mPostLikeCount = i.getLongExtra(EXTRA_POST_LIKE_COUNT, -1);
            }
            if (i.hasExtra(EXTRA_POST_VOTE_UP_COUNT) &&
                    i.hasExtra(EXTRA_POST_VOTE_DOWN_COUNT)) {
                mPostVoteUpCount = i.getLongExtra(EXTRA_POST_VOTE_UP_COUNT, -1);
                mPostVoteDownCount = i.getLongExtra(EXTRA_POST_VOTE_DOWN_COUNT, -1);
            }
            if (i.hasExtra(EXTRA_POST_IS_LIKED)) {
                mPostIsLiked = i.getBooleanExtra(EXTRA_POST_IS_LIKED, false);
            }
            if (i.hasExtra(EXTRA_POST_VOTE_STATUS)) {
                mPostVoteStatus = i.getStringExtra(EXTRA_POST_VOTE_STATUS);
            }
            if (i.hasExtra(EXTRA_POST_HASHTAGS)) {
                mPostHashTags = i.getStringArrayListExtra(EXTRA_POST_HASHTAGS);
            }

            mPostIsCommented = i.getBooleanExtra(EXTRA_POST_IS_COMMENTED, false);
            mPostCommentCount = i.getLongExtra(EXTRA_POST_COMMENT_COUNT, 0);
        }
    }

    private void setupVoteButtons() {
        mUpBtn.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
                if (isChecked) {
                    mPostVoteUpCount += 1;
                    mUpsCountTs.setText(String.valueOf(mPostVoteUpCount));

                    if (mDownBtn.isChecked()) {
                        mDownBtn.toggle();

                        if (mPostVoteDownCount != 0) {
                            mPostVoteDownCount -= 1;
                        }

                        mDownsCountTs.setText(String.valueOf(mPostVoteDownCount));
                    }

                    mPostVoteStatus = "UP";
                    getPresenter().vote(getAccessToken(), mPostId, ForumFragment.DIR_UP);
                } else {
                    if (mPostVoteUpCount != 0) {
                        mPostVoteUpCount -= 1;
                    }

                    mUpsCountTs.setText(String.valueOf(mPostVoteUpCount));

                    mPostVoteStatus = "DEFAULT";
                    getPresenter().vote(getAccessToken(), mPostId, ForumFragment.DIR_DEFAULT);
                }
            }
        });

        mDownBtn.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
                if (isChecked) {
                    mPostVoteDownCount += 1;
                    mDownsCountTs.setText(String.valueOf(mPostVoteDownCount));

                    if (mUpBtn.isChecked()) {
                        mUpBtn.toggle();

                        if (mPostVoteUpCount != 0) {
                            mPostVoteUpCount -= 1;
                        }

                        mUpsCountTs.setText(String.valueOf(mPostVoteUpCount));
                    }

                    mPostVoteStatus = "DOWN";
                    getPresenter().vote(getAccessToken(), mPostId, ForumFragment.DIR_DOWN);
                } else {
                    if (mPostVoteDownCount != 0) {
                        mPostVoteDownCount -= 1;
                    }

                    mDownsCountTs.setText(String.valueOf(mPostVoteDownCount));

                    mPostVoteStatus = "DEFAULT";
                    getPresenter().vote(getAccessToken(), mPostId, ForumFragment.DIR_DEFAULT);
                }
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // Display back arrow
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }
    }

    private void setupActionPart() {
        mUpsCountTs.setCurrentText(mPostVoteUpCount.toString());

        if (mDownsCountTs != null) {
            mDownsCountTs.setCurrentText(mPostVoteDownCount.toString());
        }
    }

    private void setupContentPart() {
        mContentTv.setText(mPostContentText);

        new PatternEditableBuilder()
                .addPattern(Pattern.compile("\\@(\\w+)"), ContextCompat.getColor(this, android.R.color.black),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
                                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                            }
                        })
                .into(mContentTv);

        if (!TextUtils.isEmpty(mPostContentImageUrl)) {
            String url = mPostContentImageUrl;
            if (BuildConfig.DEBUG) {
                url = url.replace("http://localhost:8080/_ah/", Constants.LOCAL_IMAGE_URL);
            }

            mContentImage.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mContentImage);
        }
    }

    private void setupUserInfoPart() {
        mUsernameTv.setText(mPostUsername);
        if (!mLocation.equals("null")) {
            mLocationTv.setVisibility(View.VISIBLE);
            mLocationTv.setText(mLocation);
        }

        Glide.with(this)
                .load(mPostProfileImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(mAvatarCiv);

        mTimePassedTv.setReferenceTime(mPostTimePassed);
    }

    private void setHashTags() {
        final List<String> modifiedHashes = new ArrayList<>(5);
        for (String hashTag : mPostHashTags) {
            modifiedHashes.add(getResources().getString(R.string.label_hashtag, hashTag));
        }

        mHasTagsTv.addAutoLinkMode(AutoLinkMode.MODE_HASHTAG);

        mHasTagsTv.setHashtagModeColor(mAccentColor);
        mHasTagsTv.setSelectedStateColor(mAccentColor);

        mHasTagsTv.setAutoLinkText(TextUtils.join(" ", modifiedHashes));

        mHasTagsTv.setAutoLinkOnClickListener(this);
    }

    private void setUpAndDownVoteStatus() {
        switch (mPostVoteStatus) {
            case "DEFAULT":
                mUpBtn.setChecked(false);
                mDownBtn.setChecked(false);
                break;
            case "UP":
                mUpBtn.setChecked(true);
                mDownBtn.setChecked(false);
                break;
            case "DOWN":
                mDownBtn.setChecked(true);
                mUpBtn.setChecked(false);
                break;
        }
    }

    private void setupComments() {
        if (mPostIsCommented) {
            DrawableHelper.withContext(this)
                    .withColor(R.color.colorAccent)
                    .withDrawable(R.drawable.ic_forum_white_24dp)
                    .tint()
                    .applyTo(mCommentBtn);
        } else {
            DrawableHelper.withContext(this)
                    .withColor(android.R.color.secondary_text_dark)
                    .withDrawable(R.drawable.ic_forum_white_24dp)
                    .tint()
                    .applyTo(mCommentBtn);
        }

        mCommentCountTv.setText(String.format("%s", mPostCommentCount.toString()));
    }
}
