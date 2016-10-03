package com.backpackers.android.ui.follow.delegates;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.widget.CircleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FollowDelegate extends AbsAdapterDelegate<Account, FollowDelegate.FollowViewHolder> {

    private final OnItemClickListener<Account> mOnItemClickListener;

    public FollowDelegate(OnItemClickListener<Account> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.item_search_people;
    }

    @Override
    protected boolean isForViewType(@NonNull Account item, int position) {
        return true;
    }

    @NonNull
    @Override
    protected FollowViewHolder onCreateViewHolder(ViewGroup parent, View view, List<Account> items) {
        final FollowViewHolder holder = new FollowViewHolder(view);
        setupClickListeners(holder, items);
        return holder;
    }

    @Override
    protected void onBindViewHolder(Account item, FollowViewHolder holder, int position) {
        holder.mUsernameTv.setText(item.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(item.getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(holder.mAvatarCiv);
    }

    private void setupClickListeners(final FollowViewHolder vh,
                                     final List<Account> accounts) {
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Account account = accounts.get(pos);

                    mOnItemClickListener.onItemClick(v, account);
                }
            }
        });
    }

    static class FollowViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_avatar)
        CircleImageView mAvatarCiv;

        @BindView(R.id.text_username)
        TextView mUsernameTv;

        FollowViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
