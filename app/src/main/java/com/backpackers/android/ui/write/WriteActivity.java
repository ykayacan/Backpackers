package com.backpackers.android.ui.write;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import com.backpackers.android.R;
import com.backpackers.android.internal.WeakHandler;
import com.backpackers.android.ui.location.LocationActivity;
import com.backpackers.android.ui.widget.ChipView;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.RevealBackgroundView;
import com.backpackers.android.ui.widget.ThumbView;
import com.backpackers.android.util.AnimUtils;
import com.backpackers.android.util.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class WriteActivity extends AppCompatActivity implements
        RevealBackgroundView.OnStateChangeListener, KeyboardUtils.SoftKeyboardToggleListener {

    public static final String EXTRA_MEDIA_CONTENT = "extra_media_content";
    public static final String EXTRA_TEXT_CONTENT = "extra_text_content";
    public static final String EXTRA_HASHTAGS = "extra_hashtags";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_AWARD_REP = "extra_award_rep";
    public static final String EXTRA_IS_LOCKED = "extra_is_locked";

    public static final int REQUEST_UPDATE_PAGE = 1000;

    private static final String EXTRA_REVEAL_START_LOCATION = "extra_reveal_start_location";
    private static final String EXTRA_USERNAME = "extra_username";
    private static final String EXTRA_PROFILE_IMAGE = "extra_avatar_image";

    private static final int REQUEST_SELECT_MEDIA = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_PLACE_AUTOCOMPLETE = 3;

    private static final int DEFAULT_COMPRESSION_QUALITY = 85;

    @BindView(R.id.appbar_main)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.edit_content)
    EditText mContentEt;

    @BindView(R.id.text_username)
    TextView mUsernameTv;

    @BindView(R.id.image_avatar)
    CircleImageView mAvatarIv;

    @BindView(R.id.text_location)
    TextView mLocationTv;

    @BindView(R.id.layout_small_actions)
    LinearLayout mSmallActionsLayout;

    @BindView(R.id.layout_big_actions)
    LinearLayout mBigActionsLayout;

    @BindView(R.id.layout_content_root)
    LinearLayout mContentRootLayout;

    @BindView(R.id.layout_add_media)
    LinearLayout mAddMediaLayout;

    @BindView(R.id.nested_scroll_media)
    HorizontalScrollView mNestedMedia;

    @BindView(R.id.layout_add_hashtag)
    LinearLayout mAddHashTagLayout;

    @BindView(R.id.nested_scroll_hashtags)
    HorizontalScrollView mNestedHashTagSv;

    @BindView(R.id.layout_reveal)
    RevealBackgroundView mRevealBackgroundView;

    @BindInt(android.R.integer.config_shortAnimTime)
    int mShortAnimTime;

    @BindColor(R.color.colorPrimary)
    int mColorPrimary;

    @BindColor(R.color.colorPrimaryDark)
    int mColorPrimaryDark;

    @BindString(R.string.error_max_hashtag_limit)
    String mMaxHashTagErrorString;

    private boolean mPendingIntro;

    private boolean mHasTextContent = false;
    private boolean mHasMediaContent = false;

    private WeakHandler mHandler = new WeakHandler();

    private AlertDialog mDialog;

    private String mCurrentPhotoPath;

    private ArrayList<String> mMediaPaths;
    private ArrayList<String> mHashTags = new ArrayList<>(3);

    private String mLocationName;
    private double mLat;
    private double mLong;

    public static void startWriteActivity(Fragment from, View v, String username,
                                          String profileImage, int requestCode) {
        final int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;

        final Intent i = new Intent(from.getContext(), WriteActivity.class);
        i.putExtra(EXTRA_REVEAL_START_LOCATION, startingLocation);
        i.putExtra(EXTRA_USERNAME, username);
        i.putExtra(EXTRA_PROFILE_IMAGE, profileImage);

        from.startActivityForResult(i, requestCode);
        from.getActivity().overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ButterKnife.bind(this);

        enterRevealBackground(savedInstanceState);

        setupToolbar();
        setupProfilePart();

        listenForInputChanges();

        KeyboardUtils.addKeyboardToggleListener(this, this);
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        KeyboardUtils.removeAllKeyboardToggleListeners();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_MEDIA) {
            if (resultCode == RESULT_OK) {
                handleGalleryResult(data);
            }
        } else if (requestCode == REQUEST_PLACE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                handlePlacePickerResult(data);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                handlePlacePickerError(data);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.getItem(0).setEnabled((mHasTextContent || mHasMediaContent));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_write:
                postContent();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mHasTextContent) {
            showHasContentDialog();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WriteActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick({R.id.text_add_location, R.id.image_btn_add_location})
    void addLocation() {
        KeyboardUtils.hideKeyboard(this, mContentEt);

        if (!TextUtils.isEmpty(mLocationTv.getText())) {
            showLocationOptionsDialog();
            return;
        }

        WriteActivityPermissionsDispatcher.openLocationWithCheck(this);
    }

    @OnClick({R.id.text_add_media, R.id.image_btn_add_media})
    void addMedia() {
        showMediaOptionsDialog();
    }

    @OnClick({R.id.text_add_hashtag, R.id.image_btn_add_hashtag})
    void addHashTag() {
        showHashTagDialog();
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


    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void openLocation() {
        Intent i = new Intent(this, LocationActivity.class);
        startActivityForResult(i, REQUEST_PLACE_AUTOCOMPLETE);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForLocation(final PermissionRequest request) {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.permission_location_rationale)
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

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void onLocationDenied() {
        Toast.makeText(this, R.string.permission_location_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onLocationNeverAskAgain() {
        Toast.makeText(this, R.string.permission_location_never_askagain, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            mAppBarLayout.setVisibility(View.VISIBLE);
            mContentRootLayout.setVisibility(View.VISIBLE);
            if (mPendingIntro) {
                startIntroAnimation();
            }
        } else {
            mAppBarLayout.setVisibility(View.GONE);
            mContentRootLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onToggleSoftKeyboard(final boolean isVisible) {
        mSmallActionsLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mBigActionsLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // Display back arrow
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mAppBarLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mAppBarLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                mPendingIntro = true;
                mAppBarLayout.setTranslationY(-mAppBarLayout.getHeight());
                return true;
            }
        });
    }

    // Place picker
    private void handlePlacePickerError(Intent data) {
        final Status status = PlaceAutocomplete.getStatus(this, data);
        Timber.d("Status: %s", status.getStatusMessage());
    }

    private void handlePlacePickerResult(Intent data) {
        final Bundle bundle = data.getExtras();

        mLocationName = bundle.getString(LocationActivity.EXTRA_LOCATION_NAME);
        mLat = bundle.getDouble(LocationActivity.EXTRA_LATITUDE);
        mLong = bundle.getDouble(LocationActivity.EXTRA_LONGITUDE);

        mLocationTv.setVisibility(View.VISIBLE);
        mLocationTv.setText(mLocationName);
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
            addThumbView(uri);
        } else {
            Toast.makeText(this, "Error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(Intent data) {
        Timber.e("Crop error: %s", UCrop.getError(data));
    }

    private void startCropActivity(Uri uri) {
        mMediaPaths = new ArrayList<>(3);

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


    private void postContent() {
        final Intent i = new Intent();

        if (mHasMediaContent) {
            i.putStringArrayListExtra(EXTRA_MEDIA_CONTENT, mMediaPaths);
        }

        if (mHashTags.isEmpty()) {
            showHashTagsRequiredDialog();
            return;
        }

        if (mHashTags.size() > 5) {
            showTooMuchHashTagsDialog();
            return;
        }

        String hashTags = mHashTags.toString();

        i.putExtra(EXTRA_TEXT_CONTENT, mContentEt.getText().toString());
        i.putExtra(EXTRA_HASHTAGS, hashTags.substring(1, hashTags.length() - 1));
        i.putExtra(EXTRA_LOCATION, prepareLocationString());
        i.putExtra(EXTRA_AWARD_REP, 0);
        i.putExtra(EXTRA_IS_LOCKED, false);

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private void setupProfilePart() {
        final Intent intent = getIntent();

        mUsernameTv.setText(intent.getStringExtra(EXTRA_USERNAME));
        Glide.with(this)
                .load(intent.getStringExtra(EXTRA_PROFILE_IMAGE))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(mAvatarIv);
    }

    private void listenForInputChanges() {
        mContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final boolean isEmpty = TextUtils.isEmpty(s.toString().trim());
                mHasTextContent = !isEmpty;
                invalidateOptionsMenu();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void enterRevealBackground(Bundle savedInstanceState) {
        mRevealBackgroundView.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(EXTRA_REVEAL_START_LOCATION);
            mRevealBackgroundView.getViewTreeObserver()
                    .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            mRevealBackgroundView.getViewTreeObserver().removeOnPreDrawListener(this);
                            mRevealBackgroundView.startFromLocation(startingLocation);
                            return false;
                        }
                    });
        } else {
            mRevealBackgroundView.setToFinishedFrame();
        }
    }

    private void startIntroAnimation() {
        mAppBarLayout.animate().translationY(0)
                .setDuration(400)
                .setInterpolator(AnimUtils.getDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRevealBackgroundView.setVisibility(View.GONE);
                        getWindow().setBackgroundDrawableResource(R.color.background_light);
                    }
                });
    }

    private void addThumbView(Uri uri) {
        if (mNestedMedia.getVisibility() == View.GONE) {
            mNestedMedia.setVisibility(View.VISIBLE);
        }

        final ThumbView thumbView = new ThumbView(getApplicationContext());
        thumbView.setThumbPreview(uri);
        thumbView.setListener(new ThumbView.OnDismissPreviewListener() {
            @Override
            public void onDismissPreview(View view) {
                final int index = mAddMediaLayout.indexOfChild(thumbView);

                mAddMediaLayout.removeViewAt(index);
                mMediaPaths.remove(index);

                if (mAddMediaLayout.getChildCount() == 0) {
                    mNestedMedia.setVisibility(View.GONE);
                    mHasMediaContent = false;
                }
            }
        });

        mAddMediaLayout.removeAllViews();
        mMediaPaths.clear();

        mAddMediaLayout.addView(thumbView);
        mMediaPaths.add(uri.getPath());

        mHasMediaContent = true;

        invalidateOptionsMenu();
    }

    private void addChipView(String hashTag) {
        if (mNestedHashTagSv.getVisibility() == View.GONE) {
            mNestedHashTagSv.setVisibility(View.VISIBLE);
        }

        final ChipView chipView = new ChipView(getApplicationContext());
        chipView.setHashTag(hashTag);
        chipView.setListener(new ChipView.OnDismissPreviewListener() {
            @Override
            public void onDismissPreview(View view) {
                final int index = mAddHashTagLayout.indexOfChild(chipView);

                mAddHashTagLayout.removeViewAt(index);
                mHashTags.remove(index);

                if (mAddHashTagLayout.getChildCount() == 0) {
                    mNestedHashTagSv.setVisibility(View.GONE);
                }
            }
        });

        mAddHashTagLayout.addView(chipView, 0);
        mHashTags.add(0, hashTag);
    }

    private String prepareLocationString() {
        return mLocationName + ":" + String.valueOf(mLat) + "," + String.valueOf(mLong);
    }

    // Dialogs
    private void showLocationOptionsDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_options)
                .setItems(R.array.list_add_location, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mLocationTv.setText("");
                                mLocationTv.setVisibility(View.GONE);
                                break;
                            case 1:
                                KeyboardUtils.hideKeyboard(WriteActivity.this, mContentEt);

                                WriteActivityPermissionsDispatcher.openLocationWithCheck(WriteActivity.this);
                                break;
                        }
                    }
                }).show();
    }

    private void showHashTagsRequiredDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_text_hashtag_empty)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                addHashTag();
                            }
                        }, 150);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showTooMuchHashTagsDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_text_hashtag_much)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showMediaOptionsDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_options)
                .setItems(R.array.list_add_media, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                KeyboardUtils.hideKeyboard(getApplicationContext(), mContentEt);

                                WriteActivityPermissionsDispatcher.openCameraWithCheck(WriteActivity.this);
                                break;
                            case 1:
                                KeyboardUtils.hideKeyboard(getApplicationContext(), mContentEt);

                                WriteActivityPermissionsDispatcher.openGalleryWithCheck(WriteActivity.this);
                                break;
                        }
                    }
                }).show();
    }

    private void showHasContentDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_text_content_not_null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 200);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showHashTagDialog() {
        final View hashTagView = View.inflate(this, R.layout.layout_add_hashtag, null);

        EditText hashTagEt = (EditText) hashTagView.findViewById(R.id.edit_hashtag);
        final TextInputLayout hashTagTil = (TextInputLayout) hashTagView.findViewById(R.id.layout_edit_hashtag);

        hashTagEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] hashTags = s.toString().split("\\s*,\\s*");
                if (hashTags.length > 5) {
                    hashTagTil.setError(mMaxHashTagErrorString);
                } else {
                    hashTagTil.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDialog = new AlertDialog.Builder(this)
                .setView(hashTagView)
                .setTitle(R.string.dialog_title_hashtag)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        final EditText hashtagsEt = (EditText) hashTagView.findViewById(R.id.edit_hashtag);
                        final String content = hashtagsEt.getText().toString();
                        String[] hashTags = content.trim().split("\\s*,\\s*");
                        for (String hashTag : hashTags) {
                            addChipView(hashTag.toLowerCase());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
