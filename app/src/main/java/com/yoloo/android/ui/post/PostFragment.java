package com.yoloo.android.ui.post;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoloo.android.R;
import com.yoloo.android.backend.modal.yolooApi.model.CollectionResponseAbstractPost;
import com.yoloo.android.data.repository.PostRepository;
import com.yoloo.android.data.repository.remote.PostService;
import com.yoloo.android.framework.base.BaseMvpFragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PostFragment extends BaseMvpFragment<PostView, PostPresenter> implements
        PostView, SwipeRefreshLayout.OnRefreshListener {

    private static final String EXTRA_ACCESS_TOKEN = "EXTRA_ACCESS_TOKEN";

    @BindView(R.id.recyclerview_post)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_post)
    SwipeRefreshLayout mRefreshLayout;

    private char[] mAccessToken;

    private PostAdapter mAdapter;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(char[] accessToken) {
        PostFragment fragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putCharArray(EXTRA_ACCESS_TOKEN, accessToken);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mAccessToken = bundle.getCharArray(EXTRA_ACCESS_TOKEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_post, container, false);
        ButterKnife.bind(this, view);
        mRefreshLayout.setOnRefreshListener(this);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        onLoadStarted(false);
    }

    @NonNull
    @Override
    public PostPresenter createPresenter() {
        return new PostPresenter(new PostRepository(new PostService()));
    }

    @Override
    public void onLoadStarted(boolean isPullToRefresh) {
        getPresenter().loadTimeline(isPullToRefresh, mAccessToken);
    }

    @Override
    public void onLoading(boolean isPullToRefresh) {
        mRefreshLayout.setRefreshing(isPullToRefresh);
    }

    @Override
    public void onError(Throwable e, boolean isPullToRefresh) {
        mRefreshLayout.setRefreshing(false);
        Timber.d("Error: %s", e.getMessage());
    }

    @Override
    public void onDataArrived(CollectionResponseAbstractPost data) {
        try {
            Timber.d("onDataArrived(): %s", data.getItems().get(0).toPrettyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadFinished() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        onLoadStarted(true);
    }
}
