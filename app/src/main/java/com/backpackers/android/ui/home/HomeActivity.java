package com.backpackers.android.ui.home;

import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.framework.base.BaseMvpView;
import com.backpackers.android.internal.SimpleOnTabSelectedAdapter;
import com.backpackers.android.internal.TabHolderManager;
import com.backpackers.android.internal.WeakHandler;
import com.backpackers.android.services.FcmRegistrationIntentService;
import com.backpackers.android.ui.base.BaseAuthActivity;
import com.backpackers.android.ui.home.tabs.ForumTabHolder;
import com.backpackers.android.ui.home.tabs.NotificationTabHolder;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.search.SearchActivity;
import com.backpackers.android.ui.signin.SignInActivity;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.util.AnimUtils;
import com.backpackers.android.util.BadgeDialogFactory;
import com.backpackers.android.util.PlayServicesUtil;
import com.backpackers.android.util.PrefUtils;
import com.backpackers.android.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends BaseAuthActivity<BaseMvpView, HomePresenter> implements
        BaseMvpView, NavigationView.OnNavigationItemSelectedListener {

    private static final String EXTRA_SECURITY = "EXTRA_SECURITY";

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.appbar_main)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.root_main)
    CoordinatorLayout mRootView;

    @BindView(R.id.tablayout_main)
    TabLayout mTabLayout;

    @BindView(R.id.viewpager_home)
    ViewPager mViewPager;

    @BindView(R.id.fab_main)
    FloatingActionButton mFab;

    @BindColor(R.color.colorPrimaryDark)
    int mColorPrimaryDark;

    @BindColor(R.color.colorPrimaryDarkBlue)
    int mColorPrimaryDarkBlue;

    @BindColor(R.color.colorPrimaryDarkGreen)
    int mColorPrimaryDarkGreen;

    @BindString(R.string.badge_title_welcome)
    String mWelcomeBadgeTitleString;

    @BindString(R.string.badge_content_welcome)
    String mWelcomeBadgeContentString;

    @BindString(R.string.badge_title_newbie)
    String mNewbieBadgeTitleString;

    @BindString(R.string.badge_content_newbie)
    String mNewbieBadgeContentString;

    @BindString(R.string.badge_title_first_user)
    String mFirstUserBadgeTitleString;

    @BindString(R.string.badge_content_first_user)
    String mFirstUserBadgeContentString;

    private AlertDialog mDialog;

    private TabHolderManager mTabHolderManager;

    private OnTabReselectedListener mOnTabReselectedListener;

    private WeakHandler mHandler = new WeakHandler();

    private BroadcastReceiver mBadgeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showBadge(intent);
        }
    };

    private BroadcastReceiver mProfileBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateProfileAvatar(intent);
        }
    };

    public static void startHomeActivity(Activity from) {
        final Intent i = new Intent(from, HomeActivity.class);
        i.putExtra(EXTRA_SECURITY, Constants.BASE64_CLIENT_ID);
        from.startActivity(i);
        from.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent i = getIntent();

        /*if (!i.getStringExtra(EXTRA_SECURITY).equals(Constants.BASE64_CLIENT_ID)) {
            finish();
        }*/

        if (!PrefUtils.with(this).contains(Constants.PREF_IS_BADGE_WELCOME_SHOWN + getUserId()) &&
                !isFinishing()) {
            showWelcomeBadge();
        }

        if (PlayServicesUtil.checkPlayServices(this)) {
            FcmRegistrationIntentService.start(this);
        }

        setupToolbar();
        setupDrawer();
        setupTabs();
        setupViewPager();

        setupDefaultState();
    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBadgeBroadcastReceiver, new IntentFilter("badge_event"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mProfileBroadcastReceiver, new IntentFilter("profile_update_event"));
        super.onResume();
    }

    @Override
    public void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mBadgeBroadcastReceiver);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mProfileBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        super.onDestroy();
    }

    private void setupDefaultState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, mTabHolderManager.get(0).getStatusBarColor()));
        }

        mTabLayout.setBackgroundColor(ContextCompat.getColor(this, mTabHolderManager.get(0).getBackgroundColor()));
        mAppBarLayout.setBackgroundColor(ContextCompat.getColor(this, mTabHolderManager.get(0).getBackgroundColor()));
        mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, mTabHolderManager.get(0).getBackgroundColor())));

        mToolbar.setTitle(R.string.label_search_forum);
        mFab.setImageResource(R.drawable.ic_question_mark_white_24dp);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_profile:
                mDrawer.closeDrawer(GravityCompat.START);
                openProfile();
                return true;
            case R.id.nav_settings:
                mDrawer.closeDrawer(GravityCompat.START);
                showSignOutDialog();
                return true;
        }
        return true;
    }

    @Override
    public HomePresenter createPresenter() {
        return new HomePresenter();
    }

    @OnClick(R.id.toolbar_main)
    void openSearchActivity() {
        SearchActivity.startSearchActivity(this, null);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle(R.string.label_search_yoloo);
    }

    private void setupTabs() {
        mTabHolderManager = new TabHolderManager();

        mTabHolderManager.addTabHolder(ForumTabHolder.newInstance())
                .addTabHolder(NotificationTabHolder.newInstance())
                .setupTabsWithOnlyIcon(mTabLayout);

        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedAdapter() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateSelectedTabViews(tab.getPosition());
                setSelectedTabViews(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mOnTabReselectedListener.onReselect(tab);
            }
        });
    }

    private void setupDrawer() {
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemIconTintList(null);

        setupNavProfilePart();
    }

    private void setupNavProfilePart() {
        final View header = mNavigationView.getHeaderView(0);

        final CircleImageView avatarIv = (CircleImageView) header.findViewById(R.id.image_nav_header_avatar);
        final TextView levelTv = (TextView) header.findViewById(R.id.text_avatar_level);
        final TextView usernameTv = (TextView) header.findViewById(R.id.text_nav_header_username);
        final TextView emailTv = (TextView) header.findViewById(R.id.text_nav_header_email);

        Glide.with(this)
                .load(getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(avatarIv);

        levelTv.setText("1");
        usernameTv.setText(getUsername());
        emailTv.setText(getEmail());

        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);
                openProfile();
            }
        });
    }

    private void setupViewPager() {
        final HomeAdapter adapter =
                HomeAdapter.newInstance(getSupportFragmentManager(), mTabHolderManager);

        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private void animateSelectedTabViews(final int position) {
        int colorFromBackground = ((ColorDrawable) mAppBarLayout.getBackground()).getColor();
        int colorToBackground = ContextCompat.getColor(getApplicationContext(),
                mTabHolderManager.get(position).getBackgroundColor());

        int colorFromStatusBar = 0;
        if (Utils.hasL()) {
            colorFromStatusBar = getWindow().getStatusBarColor();
        }
        int colorToStatusBar = ContextCompat.getColor(getApplicationContext(),
                mTabHolderManager.get(position).getStatusBarColor());

        ValueAnimator colorAnimationBackground =
                AnimUtils.ofArgb(colorFromBackground, colorToBackground);
        ValueAnimator colorAnimationStatusBar =
                AnimUtils.ofArgb(colorFromStatusBar, colorToStatusBar);

        colorAnimationBackground.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();

                mTabLayout.setBackgroundColor(color);
                mAppBarLayout.setBackgroundColor(color);
                mFab.setBackgroundTintList(ColorStateList.valueOf(color));
            }
        });
        colorAnimationBackground.setDuration(250);
        colorAnimationBackground.start();

        colorAnimationStatusBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(color);
                }
            }
        });

        colorAnimationStatusBar.setDuration(250);
        colorAnimationStatusBar.start();
    }

    private void openProfile() {
        new WeakHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int size = mNavigationView.getMenu().size();
                for (int i = 0; i < size; i++) {
                    mNavigationView.getMenu().getItem(i).setChecked(false);
                }
                ProfileActivity.startProfileActivity(HomeActivity.this, getUserId(), getUsername());
            }
        }, 250);
    }

    private void setSelectedTabViews(int position) {
        switch (position) {
            /*case 0:
                mToolbar.setTitle(R.string.label_search_yoloo);
                mFab.setImageResource(R.drawable.ic_checkbox_pen_outline_white_24dp);
                mFab.show();
                break;*/
            case 0:
                mToolbar.setTitle(R.string.label_search_forum);
                mFab.setImageResource(R.drawable.ic_question_mark_white_24dp);
                mFab.show();
                break;
            case 1:
                mToolbar.setTitle(R.string.label_search_forum);
                mFab.hide();
                break;
        }
    }

    private void showSignOutDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_sign_out)
                .setMessage(R.string.dialog_text_sign_out)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PrefUtils.with(HomeActivity.this)
                                .edit()
                                .remove(Constants.PREF_KEY_ACCESS_TOKEN)
                                .apply();

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(getApplicationContext(), SignInActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        }, 250);
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

    private void showWelcomeBadge() {
        final Dialog welcomeDialog = BadgeDialogFactory
                .createBadgeDialog(HomeActivity.this, mWelcomeBadgeTitleString,
                        ContextCompat.getDrawable(this, R.drawable.welcome),
                        mWelcomeBadgeContentString, new DialogInterface() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                showNewbieBadge();
                            }
                        });

        welcomeDialog.getWindow().getAttributes().windowAnimations = R.style.Widget_Yoloo_BadgeDialog;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                welcomeDialog.show();

                PrefUtils.with(getApplicationContext())
                        .edit().putBoolean(Constants.PREF_IS_BADGE_WELCOME_SHOWN + getUserId(), true)
                        .apply();
            }
        }, 650);
    }

    private void showNewbieBadge() {
        final Dialog newbieDialog = BadgeDialogFactory
                .createBadgeDialog(this, mNewbieBadgeTitleString,
                        ContextCompat.getDrawable(this, R.drawable.newbie),
                        mNewbieBadgeContentString, new DialogInterface() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                showFirstUserBadge();
                            }
                        });

        newbieDialog.getWindow().getAttributes().windowAnimations = R.style.Widget_Yoloo_BadgeDialog;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                newbieDialog.show();

                PrefUtils.with(getApplicationContext())
                        .edit().putBoolean(Constants.PREF_IS_BADGE_LEVEL_1_SHOWN + getUserId(), true)
                        .apply();
            }
        }, 250);
    }

    private void showFirstUserBadge() {
        final Dialog firstUserDialog = BadgeDialogFactory
                .createBadgeDialog(this, mFirstUserBadgeTitleString,
                        ContextCompat.getDrawable(this, R.drawable.first_user),
                        mFirstUserBadgeContentString, null);

        firstUserDialog.getWindow().getAttributes().windowAnimations = R.style.Widget_Yoloo_BadgeDialog;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                firstUserDialog.show();

                PrefUtils.with(getApplicationContext())
                        .edit().putBoolean(Constants.PREF_IS_BADGE_LEVEL_1_SHOWN + getUserId(), true)
                        .apply();
            }
        }, 250);
    }

    private void showBadge(Intent intent) {
        String title = intent.getStringExtra("extra_badge_title");
        String content = intent.getStringExtra("extra_badge_content");
        String imageUrl = intent.getStringExtra("extra_badge_image");

        final Dialog dialog = BadgeDialogFactory
                .createBadgeDialog(this, title, imageUrl, content);

        dialog.getWindow().getAttributes().windowAnimations = R.style.Widget_Yoloo_BadgeDialog;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        }, 450);
    }

    private void updateProfileAvatar(Intent intent) {
        final String imageUrl = intent.getStringExtra("EXTRA_PROFILE_IMAGE_URL");

        final View header = mNavigationView.getHeaderView(0);

        final CircleImageView avatarCiv =
                (CircleImageView) header.findViewById(R.id.image_nav_header_avatar);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(avatarCiv);
    }

    public void setOnTabReselectedListener(OnTabReselectedListener onTabReselectedListener) {
        mOnTabReselectedListener = onTabReselectedListener;
    }

    public interface OnTabReselectedListener {
        void onReselect(TabLayout.Tab tab);
    }

    private static final class HomeAdapter extends FragmentStatePagerAdapter {

        private final TabHolderManager mTabHolderManager;

        private HomeAdapter(FragmentManager fm, TabHolderManager manager) {
            super(fm);
            mTabHolderManager = manager;
        }

        static HomeAdapter newInstance(FragmentManager fm, TabHolderManager manager) {
            return new HomeAdapter(fm, manager);
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
