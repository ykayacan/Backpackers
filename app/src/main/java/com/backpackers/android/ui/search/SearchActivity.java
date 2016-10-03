package com.backpackers.android.ui.search;

import com.backpackers.android.R;
import com.backpackers.android.internal.SimpleOnTabSelectedAdapter;
import com.backpackers.android.internal.TabHolderManager;
import com.backpackers.android.ui.search.tabs.SearchHashTagTab;
import com.backpackers.android.ui.search.tabs.SearchUserTab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class SearchActivity extends AppCompatActivity {

    public static final int SEARCH_MODE_HASHTAG = 0;
    public static final int SEARCH_MODE_PEOPLE = 1;
    public static final int SEARCH_MODE_LOCATION = 2;

    private static final int SEARCH_DELAY = 150;

    private static final String EXTRA_HASHTAG = "EXTRA_HASHTAG";

    @BindView(R.id.toolbar_main)
    Toolbar mToolbar;

    @BindView(R.id.tablayout_main)
    TabLayout mTabLayout;

    @BindView(R.id.viewpager_search)
    ViewPager mViewPager;

    @BindView(R.id.edit_search)
    EditText mSearchEt;

    @BindView(R.id.image_btn_clear)
    ImageButton mClearQueryBtn;

    private List<OnSearchQueryListener> mSearchQueryListeners = new ArrayList<>(2);

    private TabHolderManager mTabHolderManager;

    private Subscription mSearchSubscription;

    private int mSearchMode;

    public static void startSearchActivity(Activity from, String hashTag) {
        final Intent i = new Intent(from, SearchActivity.class);
        i.putExtra(EXTRA_HASHTAG, hashTag);
        from.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        final Intent i = getIntent();
        String hashTag = i.getStringExtra(EXTRA_HASHTAG);

        setupToolbar();
        setupTabs();
        setupViewPager();

        mSearchMode = SEARCH_MODE_HASHTAG;

        setupSearch();

        if (hashTag != null && !hashTag.isEmpty()) {
            hashTag = hashTag.trim();
            if (hashTag.startsWith("#")) {
                hashTag = hashTag.substring(1);
            }
            mSearchEt.setText(hashTag);
            mClearQueryBtn.setVisibility(View.VISIBLE);
            queryUpdated(hashTag, mSearchMode);
        }
    }

    @Override
    protected void onDestroy() {
        if (mSearchSubscription != null && !mSearchSubscription.isUnsubscribed()) {
            mSearchSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @OnClick(R.id.image_btn_clear)
    void clearQueryText() {
        mSearchEt.setText("");
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSearch() {
        mTabLayout.addOnTabSelectedListener(new SimpleOnTabSelectedAdapter() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mSearchMode = SEARCH_MODE_HASHTAG;
                        break;
                    case 1:
                        mSearchMode = SEARCH_MODE_PEOPLE;
                        break;
                    case 2:
                        mSearchMode = SEARCH_MODE_LOCATION;
                        break;
                }
            }
        });

        mSearchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final boolean isEmpty = TextUtils.isEmpty(s);

                mClearQueryBtn.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                if (!isEmpty) {
                    mSearchSubscription = Observable.just((s))
                            .debounce(SEARCH_DELAY, TimeUnit.MILLISECONDS)
                            .subscribe(new Action1<CharSequence>() {
                                @Override
                                public void call(CharSequence query) {
                                    queryUpdated(query.toString(), mSearchMode);
                                }
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupTabs() {
        mTabHolderManager = new TabHolderManager();

        mTabHolderManager
                .addTabHolder(new SearchHashTagTab("Hashtag"))
                .addTabHolder(new SearchUserTab("People"))
                .setupTabsWithOnlyText(mTabLayout);
    }

    private void setupViewPager() {
        final SearchAdapter adapter =
                SearchAdapter.newInstance(getSupportFragmentManager(), mTabHolderManager);

        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public synchronized void registerQueryListener(OnSearchQueryListener listener) {
        mSearchQueryListeners.add(listener);
    }

    public synchronized void unregisterQueryListener(OnSearchQueryListener listener) {
        mSearchQueryListeners.remove(listener);
    }

    public synchronized void queryUpdated(String query, int searchMode) {
        for (OnSearchQueryListener l : mSearchQueryListeners) {
            l.onSearchQuery(query, searchMode);
        }
    }

    public interface OnSearchQueryListener {
        void onSearchQuery(String query, @SearchMode int mode);
    }

    @IntDef({
            SEARCH_MODE_HASHTAG,
            SEARCH_MODE_PEOPLE,
            SEARCH_MODE_LOCATION
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchMode {
    }

    private static final class SearchAdapter extends FragmentPagerAdapter {

        private final TabHolderManager mTabHolderManager;

        private SearchAdapter(FragmentManager fm, TabHolderManager manager) {
            super(fm);
            mTabHolderManager = manager;
        }

        static SearchAdapter newInstance(FragmentManager fm, TabHolderManager manager) {
            return new SearchAdapter(fm, manager);
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
