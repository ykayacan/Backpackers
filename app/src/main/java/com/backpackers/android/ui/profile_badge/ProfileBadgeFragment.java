package com.backpackers.android.ui.profile_badge;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Badge;
import com.backpackers.android.ui.profile.ProfileActivity;
import com.backpackers.android.ui.profile_badge.adapter.BadgeAdapter;
import com.backpackers.android.ui.recyclerview.GridInsetDecoration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileBadgeFragment extends Fragment implements ProfileActivity.OnBadgesLoadedListener {

    @BindView(R.id.list_profile_badge)
    RecyclerView mRecyclerView;

    private BadgeAdapter mAdapter;

    public ProfileBadgeFragment() {
        // Required empty public constructor
    }

    public static ProfileBadgeFragment newInstance() {
        final ProfileBadgeFragment fragment = new ProfileBadgeFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_badge, container, false);
        ButterKnife.bind(this, rootView);

        setupRecyclerView();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ProfileActivity) context).setOnBadgesLoadedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ProfileActivity) getActivity()).setOnBadgesLoadedListener(null);
    }

    private void setupRecyclerView() {
        final GridLayoutManager lm = new GridLayoutManager(getContext(), 4);
        mAdapter = new BadgeAdapter();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(lm);

        final SimpleItemAnimator animator = new DefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new GridInsetDecoration(getContext(), R.dimen.rv_grid_inset));

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBadgesLoaded(List<Badge> badges) {
        mAdapter.addItems(badges);
    }
}
