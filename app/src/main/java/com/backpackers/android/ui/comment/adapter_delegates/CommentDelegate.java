package com.backpackers.android.ui.comment.adapter_delegates;

import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.ui.comment.adapter.CommentViewHolder;
import com.backpackers.android.ui.listeners.OnHashTagClickListener;
import com.backpackers.android.ui.listeners.OnLikeClickListener;
import com.backpackers.android.ui.listeners.OnOptionsClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.widget.LikeButton;
import com.backpackers.android.ui.widget.PatternEditableBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.regex.Pattern;

public class CommentDelegate extends
        AbsAdapterDelegate<Comment, CommentViewHolder> {

    private final OnLikeClickListener mOnLikeClickListener;
    private final OnProfileClickListener mOnProfileClickListener;
    private final OnHashTagClickListener mOnHashTagClickListener;
    private final OnOptionsClickListener mOnOptionsClickListener;

    private final Resources mRes;

    private Context mContext;

    public CommentDelegate(OnLikeClickListener onLikeClickListener,
                           OnProfileClickListener onProfileClickListener,
                           OnHashTagClickListener onHashTagClickListener,
                           OnOptionsClickListener onOptionsClickListener,
                           Resources res) {
        mOnLikeClickListener = onLikeClickListener;
        mOnProfileClickListener = onProfileClickListener;
        mOnHashTagClickListener = onHashTagClickListener;
        mOnOptionsClickListener = onOptionsClickListener;
        mRes = res;
    }

    @Override
    protected int getLayoutResId() {
        return com.backpackers.android.R.layout.item_comment2;
    }

    @Override
    protected boolean isForViewType(@NonNull Comment item, int position) {
        return true;
    }

    @NonNull
    @Override
    protected CommentViewHolder onCreateViewHolder(ViewGroup parent, View view, List<Comment> items) {
        final CommentViewHolder vh = new CommentViewHolder(view);
        mContext = vh.itemView.getContext();
        setupCommonClickableViews(vh, items);
        return vh;
    }

    @Override
    protected void onBindViewHolder(Comment item, CommentViewHolder holder, int position) {
        holder.mUsernameTv.setText(item.getUsername());

        Glide.with(mContext)
                .load(item.getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(holder.mAvatarCiv);

        if (isOwner(item.getOwnerId())) {
            holder.mOptionsBtn.setVisibility(View.VISIBLE);
        } else {
            holder.mOptionsBtn.setVisibility(View.INVISIBLE);
        }

        holder.mContentTv.setText(item.getComment());

        new PatternEditableBuilder()
                .addPattern(Pattern.compile("\\@(\\w+)"), ContextCompat.getColor(mContext, android.R.color.black),
                        new PatternEditableBuilder.SpannableClickedListener() {
                            @Override
                            public void onSpanClicked(String text) {
                                mOnHashTagClickListener.onHashTagClick(text);
                            }
                        })
                .into(holder.mContentTv);

        holder.mTimePassedTv.setReferenceTime(item.getCreatedAt().getValue());

        holder.mLikeBtn.setLiked(item.getLiked());

        holder.mLikeTs.setCurrentText(mRes.getString(com.backpackers.android.R.string.label_likes_count, item.getLikes()));
    }

    private void setupCommonClickableViews(final CommentViewHolder vh,
                                           final List<Comment> items) {
        vh.mAvatarCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Comment comment = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, comment.getOwnerId(), comment.getUsername());
                }
            }
        });

        vh.mUsernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Comment comment = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, comment.getOwnerId(), comment.getUsername());
                }
            }
        });

        vh.mLikeBtn.setOnLikeListener(new LikeButton.OnLikeListener() {
            @Override
            public void onLiked(LikeButton likeButton) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Comment comment = items.get(pos);
                    long likes = comment.getLikes();
                    likes = likes + 1;

                    vh.mLikeTs.setText(mRes.getString(com.backpackers.android.R.string.label_likes_count, likes));

                    comment.setLiked(true);
                    comment.setLikes(likes);
                    getAdapter().notifyItemChanged(vh.getAdapterPosition());

                    mOnLikeClickListener.onLiked(comment.getId());
                }
            }

            @Override
            public void onUnLiked(LikeButton likeButton) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Comment comment = items.get(pos);
                    long likes = comment.getLikes();
                    if (likes != 0) {
                        likes = likes - 1;
                    }

                    vh.mLikeTs.setText(mRes.getString(com.backpackers.android.R.string.label_likes_count, likes));

                    comment.setLiked(false);
                    comment.setLikes(likes);
                    getAdapter().notifyItemChanged(vh.getAdapterPosition());

                    mOnLikeClickListener.onUnLiked(comment.getId());
                }
            }
        });

        vh.mOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Comment comment = items.get(pos);
                    v.setTag(pos);
                    mOnOptionsClickListener.onOptionsClick(v, comment.getPostId(), comment.getId());
                }
            }
        });
    }
}
