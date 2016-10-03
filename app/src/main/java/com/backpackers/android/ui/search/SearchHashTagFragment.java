package com.backpackers.android.ui.search;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseHashTag;
import com.backpackers.android.backend.modal.yolooApi.model.HashTag;
import com.backpackers.android.data.repository.SearchRepository;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.SearchService;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.ui.base.BaseAuthFragment;
import com.backpackers.android.ui.forum.ForumFragment;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.recyclerview.VerticalSpaceItemDecoration;
import com.backpackers.android.ui.search.adapter.SearchHashTagAdapter;
import com.backpackers.android.ui.search.adapter.SearchHashTagDiffCallback;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import butterknife.BindView;

public class SearchHashTagFragment extends BaseAuthFragment<SearchView, SearchPresenter> implements
        SearchView, SearchActivity.OnSearchQueryListener, OnItemClickListener<Object> {

    @BindView(R.id.list_search)
    RecyclerView mRecyclerView;

    private SearchHashTagAdapter mSearchHashTagAdapter;

    private SearchHashTagDiffCallback mSearchHashTagDiffCallback = new SearchHashTagDiffCallback();

    private String mHashTagCursor;

    public SearchHashTagFragment() {
        // Required empty public constructor
    }

    public static SearchHashTagFragment newInstance() {
        return new SearchHashTagFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((SearchActivity) context).registerQueryListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((SearchActivity) getActivity()).unregisterQueryListener(this);
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_search_hash_tag;
    }

    @Override
    protected SearchPresenter createPresenter() {
        return new SearchPresenter(
                new UserRepository(new UserService()),
                new SearchRepository(new SearchService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh, String cursor) {

    }

    @Override
    public void onLoading(boolean isPullToRefresh) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onDataArrived(Object data) {
        mSearchHashTagAdapter.setItems(((CollectionResponseHashTag) data).getItems());
    }

    @Override
    public void onLoadFinished() {

    }

    @Override
    public void onSearchQuery(String query, @SearchActivity.SearchMode int mode) {
        if (mode == SearchActivity.SEARCH_MODE_HASHTAG) {
            getPresenter().searchHashTags(getAccessToken(), query, "", 20);
        }
    }

    @Override
    public void onItemClick(View v, Object o) {
        if (o instanceof HashTag) {
            getActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ForumFragment.newInstance(null, ((HashTag) o).getHashTag()))
                    .commit();
        }
    }

    private void setupRecyclerView() {
        mSearchHashTagAdapter = new SearchHashTagAdapter(mSearchHashTagDiffCallback, this);

        mRecyclerView.setAdapter(mSearchHashTagAdapter);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(8));
    }
}
