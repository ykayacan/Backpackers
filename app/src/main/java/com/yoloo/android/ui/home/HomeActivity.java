package com.yoloo.android.ui.home;

import android.animation.ArgbEvaluator;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.yoloo.android.R;
import com.yoloo.android.ui.home.tabs.ForumTab;
import com.yoloo.android.ui.home.tabs.HomeTab;
import com.yoloo.android.ui.home.tabs.NotificationTab;
import com.yoloo.android.ui.home.tabs.Tab;
import com.yoloo.android.ui.util.AuthUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

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

    private char[] mAccessToken;

    private ViewPagerAdapter mAdapter;

    /**
     * Container to hold Tabs.
     */
    private SparseArrayCompat<Tab> mTabs = new SparseArrayCompat<>(3);

    private ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        mAccessToken = AuthUtil.checkIsSignedIn(this);

        setupToolbar();
        setupDrawer();
        setupTabs();
        setupViewPager();
        updateViewColors();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupTabs() {
        mTabs.append(0, HomeTab.newInstance(this, mAccessToken));
        mTabs.append(1, ForumTab.newInstance(this, mAccessToken));
        mTabs.append(2, NotificationTab.newInstance(this, mAccessToken));

        final int size = mTabs.size();

        for (int i = 0; i < size; i++) {
            mTabLayout.addTab(
                    mTabLayout.newTab().setIcon(mTabs.valueAt(i).getDrawableRes()));
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewPager() {
        mAdapter = ViewPagerAdapter.newInstance(getSupportFragmentManager(), mTabs);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private void updateViewColors() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                final int color = getColor(position, positionOffset);
                mTabLayout.setBackgroundColor(color);
                mToolbar.setBackgroundColor(color);
                mFab.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @ColorInt
    private int getColor(int position, float positionOffset) {
        final Tab tab = mTabs.get(position);
        if (position == mTabs.size() - 1) {
            return tab.getColorInt();
        }
        int startColor = tab.getColorInt();
        int endColor = mTabs.valueAt(position + 1).getColorInt();
        return (int) mArgbEvaluator.evaluate(positionOffset, startColor, endColor);
    }

    private void setStatusBarColor(@ColorInt int color) {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    private static final class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final SparseArrayCompat<Tab> tabs;

        private ViewPagerAdapter(FragmentManager fm,
                                 SparseArrayCompat<Tab> tabs) {
            super(fm);
            this.tabs = tabs;
        }

        static ViewPagerAdapter newInstance(FragmentManager fm,
                                            SparseArrayCompat<Tab> tabs) {
            return new ViewPagerAdapter(fm, tabs);
        }

        @Override
        public Fragment getItem(int position) {
            return tabs.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
    }
}
