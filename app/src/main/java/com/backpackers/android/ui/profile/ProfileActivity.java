package com.backpackers.android.ui.profile;

import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.Badge;
import com.backpackers.android.data.repository.FollowRepository;
import com.backpackers.android.data.repository.UploadRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.FollowService;
import com.backpackers.android.data.repository.remote.UploadService;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.internal.TabHolderManager;
import com.backpackers.android.ui.base.BaseAuthActivity;
import com.backpackers.android.ui.follow.FollowActivity;
import com.backpackers.android.ui.profile.tabs.BadgeTab;
import com.backpackers.android.ui.profile.tabs.PhotosTab;
import com.backpackers.android.ui.profile.tabs.PostsTab;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.RevealBackgroundView;
import com.backpackers.android.util.PrefUtils;
import com.backpackers.android.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class ProfileActivity extends BaseAuthActivity<ProfileView, ProfilePresenter> implements
        ProfileView {

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USERNAME = "extra_username";

    private static final int REQUEST_SELECT_MEDIA = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    private static final int DEFAULT_COMPRESSION_QUALITY = 85;

    private static final int NOTIFY_ID = 10;

    @BindView(R.id.text_username)
    TextView mUsernameTv;

    @BindView(R.id.btn_follow_unfollow)
    Button mFollowOrUnFollowBtn;

    @BindView(R.id.layout_content_root)
    CoordinatorLayout mContentRootLayout;

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.appbar_main)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.tablayout_main)
    TabLayout mTabLayout;

    @BindView(R.id.viewpager_profile)
    ViewPager mViewPager;

    @BindView(R.id.layout_reveal)
    RevealBackgroundView mRevealBackgroundView;

    @BindView(R.id.image_profile_bg)
    ImageView mProfileBgIv;

    @BindView(R.id.image_avatar)
    CircleImageView mAvatarCiv;

    @BindView(R.id.text_avatar_level)
    TextView mAvatarLevelTv;

    @BindView(R.id.text_short_desc)
    TextView mShortDescTv;

    @BindView(R.id.text_posts_count)
    TextView mPostCountTv;

    @BindView(R.id.text_follower_count)
    TextView mFollowerCountTv;

    @BindView(R.id.text_followee_count)
    TextView mFolloweeCountTv;

    @BindColor(R.color.colorPrimary)
    int mColorPrimary;

    @BindColor(R.color.colorPrimaryDark)
    int mColorPrimaryDark;

    @BindString(R.string.label_follow)
    String mFollowString;

    @BindString(R.string.label_unfollow)
    String mUnFollowString;

    private String mUserId;
    private String mUsername;

    private boolean mIsFollowing;

    private boolean mIsProfileUpdate;

    private TabHolderManager mTabHolderManager;

    private AlertDialog mDialog;

    private String mCurrentPhotoPath;

    private NotificationManager mNotificationManager;

    private NotificationCompat.Builder mNotificationBuilder;

    private OnBadgesLoadedListener mOnBadgesLoadedListener;

    public static void startProfileActivity(Context context, String userId, String username) {
        final Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        i.putExtra(EXTRA_USERNAME, username);

        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Intent i = getIntent();
        mUserId = i.getStringExtra(EXTRA_USER_ID);
        mUsername = i.getStringExtra(EXTRA_USERNAME);

        mFollowOrUnFollowBtn.setVisibility(getUserId().equals(mUserId) ? View.GONE : View.VISIBLE);

        setupToolbar();
        setupTabs();
        setupViewPager();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getUserId().equals(mUserId)) {
            getPresenter().getUserDetail(getAccessToken());
        } else {
            getPresenter().getUserDetail(getAccessToken(), mUserId);
        }
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_MEDIA) {
            if (resultCode == RESULT_OK) {
                handleGalleryResult(data);
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                handleCropResult(data);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                handleCropError(data);
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                handleCameraResult();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ProfileActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected ProfilePresenter createPresenter() {
        return new ProfilePresenter(
                new UserRepository(new UserService()),
                new FollowRepository(new FollowService()),
                new UploadRepository(new UploadService()));
    }

    @Override
    public void onProfile(Account account) {
        if (getUserId().equals(account.getId()) &&
                !getProfileImageUrl().equals(account.getProfileImageUrl())) {
            mIsProfileUpdate = true;

            Intent i = new Intent("profile_update_event");
            i.putExtra("EXTRA_PROFILE_IMAGE_URL", account.getProfileImageUrl());

            LocalBroadcastManager.getInstance(this).sendBroadcast(i);

            PrefUtils.with(this).edit()
                    .putString(Constants.PREF_KEY_PROFILE_IMAGE_URL, account.getProfileImageUrl())
                    .apply();
        } else {
            mIsProfileUpdate = false;
        }

        Glide.with(this)
                .load(account.getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .into(mAvatarCiv);

        mAvatarLevelTv.setText("1");
        mUsernameTv.setText(account.getUsername());
        mShortDescTv.setText("");
        mPostCountTv.setText(StringUtils.format(account.getQuestions()));
        mFollowerCountTv.setText(StringUtils.format(account.getFollowers()));
        mFolloweeCountTv.setText(StringUtils.format(account.getFollowees()));

        mFollowOrUnFollowBtn.setText(account.getFollowing() ? mUnFollowString : mFollowString);
        mIsFollowing = account.getFollowing();

        if (!mIsProfileUpdate) {
            mOnBadgesLoadedListener.onBadgesLoaded(account.getBadges());
        }
    }

    @Override
    public void onUserFollowed() {
        mFollowOrUnFollowBtn.setText(getString(R.string.label_unfollow));
        showSnackbar(getString(R.string.label_followed));
    }

    @Override
    public void onUserUnFollowed() {
        mFollowOrUnFollowBtn.setText(getString(R.string.label_follow));
        showSnackbar(getString(R.string.label_unfollowed));
    }

    @Override
    public void onShowUploadNotification() {
        mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(this)
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

    @OnClick(R.id.btn_follow_unfollow)
    void followOrUnFollow() {
        if (mIsFollowing) {
            getPresenter().unFollow(getAccessToken(), mUserId);
        } else {
            getPresenter().follow(getAccessToken(), mUserId);
        }

        mIsFollowing = !mIsFollowing;
    }

    @OnClick(R.id.image_avatar)
    void changeImage() {
        if (mUserId.equals(getUserId())) {
            showChangeImageDialog();
        }
    }

    @OnClick({R.id.text_follower_count, R.id.text_follower})
    void showFollowers() {
        FollowActivity.open(this, FollowActivity.MODE_FOLLOWERS, mUserId);
    }

    @OnClick({R.id.text_followee_count, R.id.text_followee})
    void showFollowings() {
        FollowActivity.open(this, FollowActivity.MODE_FOLOWINGS, mUserId);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);

            ab.setTitle(mUsername);
        }
    }

    private void setupTabs() {
        mTabHolderManager = new TabHolderManager();

        mTabHolderManager.addTabHolder(new PhotosTab(mUserId))
                .addTabHolder(new BadgeTab(mUserId))
                .addTabHolder(new PostsTab(mUserId))
                .setupTabsWithOnlyIcon(mTabLayout);
    }

    private void setupViewPager() {
        final ProfileAdapter adapter =
                ProfileAdapter.newInstance(getSupportFragmentManager(), mTabHolderManager);

        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private void showSnackbar(String text) {
        Snackbar.make(mContentRootLayout, text, Snackbar.LENGTH_SHORT).show();
    }

    private void showChangeImageDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_options)
                .setItems(R.array.list_add_media, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                ProfileActivityPermissionsDispatcher.openCameraWithCheck(ProfileActivity.this);
                                break;
                            case 1:
                                ProfileActivityPermissionsDispatcher.openGalleryWithCheck(ProfileActivity.this);
                                break;
                        }
                    }
                }).show();
    }

    // Permissions
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.label_select_picture)),
                REQUEST_SELECT_MEDIA);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForGallery(final PermissionRequest request) {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.permission_gallery_rationale)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onGalleryDenied() {
        Toast.makeText(this, R.string.permission_gallery_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onGalleryNeverAskAgain() {
        Toast.makeText(this, R.string.permission_gallery_never_askagain, Toast.LENGTH_SHORT).show();
    }


    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE})
    void openCamera() {
        dispatchTakePictureIntent();
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForCamera(final PermissionRequest request) {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE})
    void onCameraDenied() {
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE})
    void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
    }

    // Select image from gallery
    private void handleGalleryResult(Intent data) {
        final Uri uri = data.getData();
        if (uri != null) {
            startCropActivity(uri);
        }
    }

    private void handleCropResult(Intent data) {
        final Uri uri = UCrop.getOutput(data);
        if (uri != null) {
            changeAvatarImage(uri);
        } else {
            Toast.makeText(this, "Error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(Intent data) {
        Timber.e("Crop error: %s", UCrop.getError(data));
    }

    private void startCropActivity(Uri uri) {
        final String croppedImageName = UUID.randomUUID().toString() + ".jpeg";
        final Uri destUri = Uri.fromFile(new File(getCacheDir(), croppedImageName));

        final UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(DEFAULT_COMPRESSION_QUALITY);
        options.setToolbarColor(mColorPrimary);
        options.setStatusBarColor(mColorPrimaryDark);
        options.setToolbarTitle(getString(R.string.title_activity_crop));

        UCrop.of(uri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .start(this);
    }

    // Camera
    private void handleCameraResult() {
        final Uri uri = Uri.parse(mCurrentPhotoPath);
        if (uri != null) {
            startCropActivity(uri);
        }
    }

    private File createImageFile() {
        String timeStamp = SimpleDateFormat.getDateInstance().format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        File storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Timber.d("Can't create directory to save image.");
        }

        File image = new File(storageDir.getPath() + File.separator + imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();

        Timber.d("Path: %s", mCurrentPhotoPath);

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void changeAvatarImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .into(mAvatarCiv);

        getPresenter().updateUser(getAccessToken(), getUserId(), uri.getPath());
    }

    public void setOnBadgesLoadedListener(OnBadgesLoadedListener onBadgesLoadedListener) {
        mOnBadgesLoadedListener = onBadgesLoadedListener;
    }

    public interface OnBadgesLoadedListener {
        void onBadgesLoaded(List<Badge> badges);
    }

    private static final class ProfileAdapter extends FragmentStatePagerAdapter {

        private final TabHolderManager mTabHolderManager;

        private ProfileAdapter(FragmentManager fm, TabHolderManager manager) {
            super(fm);
            mTabHolderManager = manager;
        }

        static ProfileAdapter newInstance(FragmentManager fm, TabHolderManager manager) {
            return new ProfileAdapter(fm, manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mTabHolderManager.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return mTabHolderManager.getTabHolders().size();
        }
    }
}
