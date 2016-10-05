package com.backpackers.android.ui.forum.adapter;

import com.backpackers.android.R;
import com.backpackers.android.ui.widget.CheckableImageButton;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.RelativeTimeTextView;
import com.luseen.autolinklibrary.AutoLinkTextView;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForumPostViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.image_avatar)
    public CircleImageView mAvatarCiv;

    @BindView(R.id.text_username)
    public TextView mUsernameTv;

    @BindView(R.id.text_location)
    @Nullable
    public TextView mLocationTv;

    @BindView(R.id.text_time)
    public RelativeTimeTextView mTimePassedTv;

    @BindView(R.id.text_content)
    public TextView mContentTv;

    @BindView(R.id.image_content)
    @Nullable
    public ImageView mContentImage;

    @BindView(R.id.text_hashTags)
    public AutoLinkTextView mHasTagsTv;

    @BindView(R.id.image_btn_vote_up)
    public CheckableImageButton mUpBtn;

    @BindView(R.id.ts_ups)
    public TextSwitcher mUpsCountTs;

    @BindView(R.id.image_btn_vote_down)
    @Nullable
    public CheckableImageButton mDownBtn;

    @BindView(R.id.ts_downs)
    @Nullable
    public TextSwitcher mDownsCountTs;

    @BindView(R.id.image_btn_comment)
    public ImageButton mCommentBtn;

    @BindView(R.id.text_comments)
    public TextView mCommentCountTv;

    public ForumPostViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
