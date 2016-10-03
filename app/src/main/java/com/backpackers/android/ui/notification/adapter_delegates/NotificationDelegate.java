package com.backpackers.android.ui.notification.adapter_delegates;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Notification;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.notification.OnFollowBackListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.widget.CircleImageView;
import com.backpackers.android.ui.widget.PatternEditableBuilder;
import com.backpackers.android.ui.widget.RelativeTimeTextView;
import com.bumptech.glide.Glide;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationDelegate extends AbsAdapterDelegate<Notification, NotificationDelegate.NotificationViewHolder> {

    private final Resources mRes;
    private final OnProfileClickListener mOnProfileClickListener;
    private final OnFollowBackListener mOnFollowBackListener;
    private final OnCommentsClickListener mOnCommentsClickListener;

    public NotificationDelegate(Resources res, OnProfileClickListener onProfileClickListener,
                                OnFollowBackListener onFollowBackListener,
                                OnCommentsClickListener onCommentsClickListener) {
        mRes = res;
        mOnProfileClickListener = onProfileClickListener;
        mOnFollowBackListener = onFollowBackListener;
        mOnCommentsClickListener = onCommentsClickListener;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.item_notification;
    }

    @Override
    protected boolean isForViewType(@NonNull Notification item, int position) {
        return true;
    }

    @NonNull
    @Override
    protected NotificationViewHolder onCreateViewHolder(ViewGroup parent, View view, List<Notification> items) {
        final NotificationViewHolder holder = new NotificationViewHolder(view);
        setupCommonClickableViews(holder, items);
        return holder;
    }

    @Override
    protected void onBindViewHolder(Notification item, NotificationViewHolder holder, int position) {
        holder.mTimeTv.setReferenceTime(item.getCreatedAt().getValue());
        holder.mUsernameTv.setText(item.getUsername());
        holder.mActionTv.setText(mRes.getString(getActionStringRes(item.getAction())));

        holder.mContentTv.setVisibility(TextUtils.isEmpty(item.getContent()) ? View.GONE : View.VISIBLE);
        holder.mContentTv.setText(item.getContent());
        holder.mActionBtn.setVisibility(item.getAction().equals("FOLLOW") ? View.VISIBLE : View.GONE);

        new PatternEditableBuilder()
                .addPattern(Pattern.compile("\\@(\\w+)"),
                        ContextCompat.getColor(holder.mContentTv.getContext(), android.R.color.black),
                        null)
                .into(holder.mContentTv);

        Glide.with(holder.mAvatarCiv.getContext())
                .load(item.getProfileImageUrl())
                .into(holder.mAvatarCiv);
    }

    private int getActionStringRes(String action) {
        switch (action) {
            case "COMMENT":
                return R.string.label_action_comment;
            case "MENTION":
                return R.string.label_action_mention;
            case "FOLLOW":
                return R.string.label_action_follow;
            default:
                return -1;
        }
    }

    private void setupCommonClickableViews(final NotificationViewHolder holder,
                                           final List<Notification> items) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Notification notification = items.get(pos);

                    if (notification.getAction().equals("FOLLOW")) {
                        mOnProfileClickListener.onProfileClick(v, notification.getSenderId(),
                                notification.getUsername());
                    } else if (notification.getAction().equals("COMMENT")) {
                        mOnCommentsClickListener.onCommentClick(v, notification.getWebsafePostId(),
                                notification.getReceiverId());
                    }
                }
            }
        });

        holder.mActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Notification notification = items.get(pos);

                    if (notification.getAction().equals("FOLLOW")) {
                        mOnFollowBackListener.onFollowBack(notification.getSenderId());
                    }
                }
            }
        });
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_time)
        RelativeTimeTextView mTimeTv;

        @BindView(R.id.image_avatar)
        CircleImageView mAvatarCiv;

        @BindView(R.id.text_username)
        TextView mUsernameTv;

        @BindView(R.id.text_action)
        TextView mActionTv;

        @BindView(R.id.text_content)
        TextView mContentTv;

        @BindView(R.id.image_btn_action_symbol)
        ImageButton mActionBtn;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
