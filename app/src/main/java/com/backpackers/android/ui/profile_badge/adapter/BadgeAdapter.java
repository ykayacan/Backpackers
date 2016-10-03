package com.backpackers.android.ui.profile_badge.adapter;

import com.backpackers.android.backend.modal.yolooApi.model.Badge;
import com.backpackers.android.util.BadgeDialogFactory;
import com.bumptech.glide.Glide;

import android.app.Dialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> mItems = new ArrayList<>();

    public void addItems(List<Badge> badges) {
        mItems.addAll(badges);
        if (mItems.size() < 5) {
            int remainder = 32 - mItems.size();
            for (int i = 0; i < remainder; i++) {
                mItems.add(new Badge());
            }
        }

        notifyItemRangeInserted(0, mItems.size());
    }

    @Override
    public BadgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.backpackers.android.R.layout.item_badge_grid, parent, false);
        BadgeViewHolder vh = new BadgeViewHolder(view);
        setupClickListeners(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(BadgeViewHolder holder, int position) {
        final Badge badge = mItems.get(position);

        Glide.with(holder.itemView.getContext())
                .load(badge.getImageUrl())
                .placeholder(AppCompatResources
                        .getDrawable(holder.itemView.getContext(), com.backpackers.android.R.drawable.badge_placeholder))
                .into(holder.mBadgeIv);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void setupClickListeners(final BadgeViewHolder vh) {
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Badge badge = mItems.get(pos);

                    if (badge.getName() != null) {
                        Dialog dialog = BadgeDialogFactory
                                .createBadgeDialog(vh.itemView.getContext(), badge.getName(),
                                        badge.getImageUrl(), badge.getContent());

                        dialog.getWindow().getAttributes().windowAnimations = com.backpackers.android.R.style.Widget_Yoloo_BadgeDialog;

                        dialog.show();
                    }
                }
            }
        });
    }

    static final class BadgeViewHolder extends RecyclerView.ViewHolder {

        @BindView(com.backpackers.android.R.id.image_badge_grid)
        ImageView mBadgeIv;

        public BadgeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
