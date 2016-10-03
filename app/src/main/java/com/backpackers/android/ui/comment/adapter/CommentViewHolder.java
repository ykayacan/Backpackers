package com.backpackers.android.ui.comment.adapter;

import com.backpackers.android.ui.widget.LikeButton;
import com.backpackers.android.R;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.RelativeTimeTextView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image_avatar)
    public CircleImageView mAvatarCiv;

    @BindView(R.id.text_username)
    public TextView mUsernameTv;

    @BindView(R.id.image_btn_options)
    public ImageButton mOptionsBtn;

    @BindView(R.id.text_time)
    public RelativeTimeTextView mTimePassedTv;

    @BindView(R.id.text_content)
    public TextView mContentTv;

    @BindView(R.id.btn_like)
    public LikeButton mLikeBtn;

    @BindView(R.id.ts_likes)
    public TextSwitcher mLikeTs;

    public CommentViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
