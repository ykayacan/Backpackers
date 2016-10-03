package com.backpackers.android.ui.timeline.adapter_delegates;

import com.google.api.client.util.ArrayMap;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnLikeClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.timeline.adapter.TimelinePostViewHolder;
import com.backpackers.android.ui.widget.LikeButton;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class TimelineVideoDelegate extends
        AbsAdapterDelegate<AbstractPost, TimelinePostViewHolder> {

    private final OnLikeClickListener mOnLikeClickListener;
    private final OnCommentsClickListener mOnCommentsClickListener;
    private final OnProfileClickListener mOnProfileClickListener;

    private final Resources mRes;

    public TimelineVideoDelegate(OnLikeClickListener onLikeClickListener,
                                 OnCommentsClickListener onCommentsClickListener,
                                 OnProfileClickListener onProfileClickListener,
                                 Resources res) {
        mOnLikeClickListener = onLikeClickListener;
        mOnCommentsClickListener = onCommentsClickListener;
        mOnProfileClickListener = onProfileClickListener;
        mRes = res;
    }

    @Override
    public int getLayoutResId() {
        return com.backpackers.android.R.layout.item_timeline_video;
    }

    @Override
    public boolean isForViewType(@NonNull AbstractPost item, int position) {
        return item.getType().equals("normal") && item.getMedias() != null
                && item.getMedias().get(0).getMime().contains("video");
    }

    @NonNull
    @Override
    public TimelinePostViewHolder onCreateViewHolder(ViewGroup parent, View view, List<AbstractPost> items) {
        final TimelinePostViewHolder vh = new TimelinePostViewHolder(view);
        setupCommonClickableViews(vh, items);
        return vh;
    }

    @Override
    public void onBindViewHolder(AbstractPost item, TimelinePostViewHolder holder, int position) {
        holder.mUsernameTv.setText(item.getUsername());

        holder.mContentTv.setText(item.getContent());

        Object locationField = item.get("locations");
        if (locationField != null) {
            @SuppressWarnings("unchecked")
            List<ArrayMap<Object, Object>> locations = (List<ArrayMap<Object, Object>>) locationField;
            holder.mLocationTv.setVisibility(View.VISIBLE);
            holder.mLocationTv.setText(locations.get(0).get("name").toString());
        } else {
            holder.mLocationTv.setVisibility(View.GONE);
        }

        holder.mTimePassedTv.setReferenceTime(item.getCreatedAt().getValue());

        Object likesField = item.get("likes");
        if (likesField != null) {
            long likes = Long.parseLong(likesField.toString());
            holder.mLikeTs.setCurrentText(mRes.getString(com.backpackers.android.R.string.label_likes_count, likes));
        }

        Object commentsField = item.get("comments");
        if (commentsField != null) {
            long comments = Long.parseLong(commentsField.toString());
            holder.mCommentCountTv.setText(mRes.getString(com.backpackers.android.R.string.label_comments_count, comments));
        }

        Object likedField = item.get("liked");
        if (likedField != null) {
            boolean isLiked = (boolean) likedField;
            holder.mLikeBtn.setLiked(isLiked);
        } else {
            holder.mLikeBtn.setLiked(false);
        }
    }

    private void setupCommonClickableViews(final TimelinePostViewHolder vh,
                                           final List<AbstractPost> items) {
        vh.mLikeBtn.setOnLikeListener(new LikeButton.OnLikeListener() {
            @Override
            public void onLiked(LikeButton likeButton) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final AbstractPost post = items.get(pos);
                    long likes = Long.parseLong(post.get("likes").toString());

                    vh.mLikeTs.setText(mRes.getString(com.backpackers.android.R.string.label_likes_count, likes + 1));

                    mOnLikeClickListener.onLiked(post.getId());
                }
            }

            @Override
            public void onUnLiked(LikeButton likeButton) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final AbstractPost post = items.get(pos);
                    long likes = Long.parseLong(post.get("likes").toString());
                    if (likes != 0) {
                        likes = likes - 1;
                    }

                    vh.mLikeTs.setText(mRes.getString(com.backpackers.android.R.string.label_likes_count, likes));

                    mOnLikeClickListener.onUnLiked(post.getId());
                }
            }
        });

        vh.mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final AbstractPost post = items.get(pos);
                    mOnCommentsClickListener.onCommentClick(v, post.getId(), post.getOwnerId());
                }
            }
        });

        vh.mAvatarCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final AbstractPost post = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, post.getOwnerId(), post.getUsername());
                }
            }
        });

        vh.mUsernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final AbstractPost post = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, post.getOwnerId(), post.getUsername());
                }
            }
        });
    }
}
