package com.backpackers.android.ui.photo;

import com.backpackers.android.BuildConfig;
import com.backpackers.android.Constants;
import com.backpackers.android.internal.WeakHandler;
import com.backpackers.android.ui.widget.PinchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoActivity extends AppCompatActivity {

    private static final String EXTRA_PHOTO_URL = "extra_photo_url";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    @BindView(com.backpackers.android.R.id.image_fullscreen)
    PinchImageView mFullscreenIv;

    @BindView(com.backpackers.android.R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @BindView(com.backpackers.android.R.id.toolbar)
    Toolbar mToolbar;

    private final WeakHandler mHideHandler = new WeakHandler();

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mFullscreenIv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            mAppBarLayout.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    };

    /**
     * All UI items are visible by default
     */
    private boolean mVisible = true;

    public static void startPhotoActivity(Context context, String photoUrl,
                                          @Nullable ActivityOptionsCompat options) {
        final Intent i = new Intent(context, PhotoActivity.class);
        i.putExtra(PhotoActivity.EXTRA_PHOTO_URL, photoUrl);

        if (options != null) {
            context.startActivity(i, options.toBundle());
        } else {
            context.startActivity(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.backpackers.android.R.layout.activity_photo);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setupToolbar();

        final Intent intent = getIntent();
        String photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL);

        if (BuildConfig.DEBUG) {
            photoUrl = photoUrl.replace("http://localhost:8080/_ah/", Constants.LOCAL_IMAGE_URL);
        }

        Glide.with(this)
                .load(photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mFullscreenIv);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            supportFinishAfterTransition();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(com.backpackers.android.R.id.image_fullscreen)
    void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mAppBarLayout.animate()
                .translationY(-mAppBarLayout.getBottom())
                .setInterpolator(new AccelerateInterpolator())
                .start();
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mFullscreenIv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void setupToolbar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // Display back arrow
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }
    }
}
