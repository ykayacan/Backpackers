package com.yoloo.android.ui.post;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yoloo.android.R;
import com.yoloo.android.ui.widget.CircleImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_profile_post_item)
        CircleImageView mAvatarCiv;

        @BindView(R.id.text_username_post_item)
        TextView mUsernameTv;

        @BindView(R.id.text_location_post_item)
        TextView mLocationTv;

        @BindView(R.id.text_time_post_item)
        TextView mTimePassedTv;

        public PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }
    }
}
