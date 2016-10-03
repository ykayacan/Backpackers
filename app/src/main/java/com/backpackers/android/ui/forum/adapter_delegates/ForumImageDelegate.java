package com.backpackers.android.ui.forum.adapter_delegates;

import com.backpackers.android.BuildConfig;
import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.MediaDetail;
import com.backpackers.android.backend.modal.yolooApi.model.MediaObject;
import com.backpackers.android.ui.forum.ForumFragment;
import com.backpackers.android.ui.forum.adapter.ForumPostViewHolder;
import com.backpackers.android.ui.listeners.OnCommentsClickListener;
import com.backpackers.android.ui.listeners.OnContentImageClickListener;
import com.backpackers.android.ui.listeners.OnHashTagClickListener;
import com.backpackers.android.ui.listeners.OnProfileClickListener;
import com.backpackers.android.ui.listeners.OnReadMoreClickListener;
import com.backpackers.android.ui.listeners.OnVoteActionClickListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.widget.CheckableImageButton;
import com.backpackers.android.ui.widget.ReadMoreTextView;
import com.backpackers.android.util.DrawableHelper;
import com.backpackers.android.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ForumImageDelegate extends AbsAdapterDelegate<ForumPost, ForumPostViewHolder> {

    private final OnVoteActionClickListener mOnVoteActionClickListener;
    private final OnProfileClickListener mOnProfileClickListener;
    private final OnContentImageClickListener mOnContentImageClickListener;
    private final OnCommentsClickListener mOnCommentsClickListener;
    private final OnHashTagClickListener mOnHashTagClickListener;
    private final OnReadMoreClickListener mOnReadMoreClickListener;

    private final Resources mRes;

    private boolean mIsBigLayout;

    private Context mContext;

    public ForumImageDelegate(OnVoteActionClickListener onVoteActionClickListener,
                              OnProfileClickListener onProfileClickListener,
                              OnContentImageClickListener onContentImageClickListener,
                              OnCommentsClickListener onCommentsClickListener,
                              OnHashTagClickListener onHashTagClickListener,
                              OnReadMoreClickListener onReadMoreClickListener,
                              Resources res, boolean isBigLayout) {
        mOnVoteActionClickListener = onVoteActionClickListener;
        mOnProfileClickListener = onProfileClickListener;
        mOnContentImageClickListener = onContentImageClickListener;
        mOnCommentsClickListener = onCommentsClickListener;
        mOnHashTagClickListener = onHashTagClickListener;
        mOnReadMoreClickListener = onReadMoreClickListener;
        mRes = res;
        mIsBigLayout = isBigLayout;
    }

    @Override
    public int getLayoutResId() {
        if (mIsBigLayout) {
            return R.layout.item_forum_big_image;
        }
        return R.layout.item_forum_small_image2;
    }

    @Override
    public boolean isForViewType(@NonNull ForumPost item, int position) {
        return item.getMedias() != null && item.getMedias().get(0).getMime().contains("image");
    }

    @NonNull
    @Override
    public ForumPostViewHolder onCreateViewHolder(ViewGroup parent, View view, List<ForumPost> items) {
        final ForumPostViewHolder vh = new ForumPostViewHolder(view);
        mContext = vh.itemView.getContext();
        setupCommonClickableViews(vh, items);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ForumPost item, final ForumPostViewHolder holder, int position) {
        holder.mUsernameTv.setText(item.getUsername());

        Glide.with(mContext)
                .load(item.getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(holder.mAvatarCiv);

        holder.mTimePassedTv.setReferenceTime(item.getCreatedAt().getValue());

        bindLocation(item, holder);

        holder.mContentTv.setText(item.getContent());

        bindContentImage(item, holder);

        bindHashTags(item, holder);

        bindUpAndDownVoteStatus(item, holder);

        holder.mUpsCountTs.setCurrentText(item.getUps().toString());

        if (holder.mDownsCountTs != null) {
            holder.mDownsCountTs.setCurrentText(item.getDowns().toString());
        }

        bindComments(item, holder);

        holder.mCommentCountTv.setText(StringUtils.format(item.getComments()));
    }

    private void bindLocation(ForumPost item, ForumPostViewHolder holder) {
        if (item.getLocation() != null &&
                !item.getLocation().getName().equals("null") &&
                holder.mLocationTv != null) {
            holder.mLocationTv.setVisibility(View.VISIBLE);
            holder.mLocationTv.setText(item.getLocation().getName());
        }
    }

    private void bindContentImage(ForumPost item, ForumPostViewHolder holder) {
        final MediaDetail detail = item.getMedias().get(0).getDetail();
        final MediaObject mediaObject = getDetail(mIsBigLayout, detail);
        String url = mediaObject.getUrl();
        if (BuildConfig.DEBUG) {
            url = url.replace("http://localhost:8080/_ah/", Constants.LOCAL_IMAGE_URL);
        }

        if (holder.mContentImage != null) {
            Glide.with(mContext)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .into(holder.mContentImage);
        }
    }

    private void bindHashTags(ForumPost item, ForumPostViewHolder holder) {
        final List<String> modifiedHashes = new ArrayList<>(5);
        for (String hashTag : item.getHashtags()) {
            modifiedHashes.add(mRes.getString(R.string.label_hashtag, hashTag));
        }

        holder.mHasTagsTv.addAutoLinkMode(AutoLinkMode.MODE_HASHTAG);

        holder.mHasTagsTv.setHashtagModeColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));
        holder.mHasTagsTv.setSelectedStateColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));

        holder.mHasTagsTv.setAutoLinkText(TextUtils.join(" ", modifiedHashes));
    }

    private void bindComments(ForumPost item, ForumPostViewHolder holder) {
        if (item.getCommented()) {
            DrawableHelper.withContext(mContext)
                    .withColor(R.color.colorAccent)
                    .withDrawable(mIsBigLayout
                            ? R.drawable.ic_forum_white_24dp
                            : R.drawable.ic_forum_white_32dp)
                    .tint()
                    .applyTo(holder.mCommentBtn);
        } else {
            DrawableHelper.withContext(mContext)
                    .withColor(android.R.color.secondary_text_dark)
                    .withDrawable(mIsBigLayout
                            ? R.drawable.ic_forum_white_24dp
                            : R.drawable.ic_forum_white_32dp)
                    .tint()
                    .applyTo(holder.mCommentBtn);
        }
    }

    private void bindUpAndDownVoteStatus(ForumPost item, ForumPostViewHolder holder) {
        switch (item.getStatus()) {
            case "DEFAULT":
                holder.mUpBtn.setChecked(false);
                if (holder.mDownBtn != null) {
                    holder.mDownBtn.setChecked(false);
                }
                break;
            case "UP":
                holder.mUpBtn.setChecked(true);
                if (holder.mDownBtn != null) {
                    holder.mDownBtn.setChecked(false);
                }
                break;
            case "DOWN":
                if (holder.mDownBtn != null) {
                    holder.mDownBtn.setChecked(true);
                }
                holder.mUpBtn.setChecked(false);
                break;
        }
    }

    private void setupCommonClickableViews(final ForumPostViewHolder vh,
                                           final List<ForumPost> items) {
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    mOnReadMoreClickListener.onReadMoreClick(v, pos, post);
                }
            }
        });

        vh.mAvatarCiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, post.getOwnerId(), post.getUsername());
                }
            }
        });

        vh.mUsernameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    mOnProfileClickListener.onProfileClick(v, post.getOwnerId(), post.getUsername());
                }
            }
        });

        vh.mContentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    mOnReadMoreClickListener.onReadMoreClick(v, pos, post);
                }
            }
        });

        vh.mContentTv.setOnReadMoreListener(new ReadMoreTextView.OnReadMoreListener() {
            @Override
            public void onReadMore(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    mOnReadMoreClickListener.onReadMoreClick(v, pos, post);
                }
            }
        });

        if (vh.mContentImage != null) {
            vh.mContentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final ForumPost post = items.get(pos);
                        mOnContentImageClickListener.onContentImageClick(
                                v, post.getMedias().get(0).getDetail().getStd().getUrl());
                    }
                }
            });
        }

        vh.mUpBtn.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);

                    if (isChecked) {
                        long ups = post.getUps() + 1;

                        post.setUps(ups);
                        post.setStatus("UP");

                        vh.mUpsCountTs.setText(String.valueOf(ups));

                        if (vh.mDownBtn != null &&
                                vh.mDownBtn.isChecked() &&
                                vh.mDownsCountTs != null) {
                            vh.mDownBtn.toggle();

                            long downs = post.getDowns();
                            if (downs != 0) {
                                downs -= 1;
                            }

                            post.setDowns(downs);
                            vh.mDownsCountTs.setText(String.valueOf(downs));
                        }

                        mOnVoteActionClickListener.onVoteAction(post.getId(), ForumFragment.DIR_UP);
                    } else {
                        long ups = post.getUps();
                        if (ups != 0) {
                            ups -= 1;
                        }

                        post.setUps(ups);
                        post.setStatus("DEFAULT");

                        vh.mUpsCountTs.setText(String.valueOf(ups));

                        mOnVoteActionClickListener.onVoteAction(post.getId(), ForumFragment.DIR_DEFAULT);
                    }

                    getAdapter().notifyItemChanged(pos, post);
                }

            }
        });

        if (vh.mDownBtn != null && vh.mDownsCountTs != null) {
            vh.mDownBtn.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CheckableImageButton button, boolean isChecked) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final ForumPost post = items.get(pos);

                        if (isChecked) {
                            long downs = post.getDowns() + 1;

                            post.setDowns(downs);
                            post.setStatus("DOWN");

                            vh.mDownsCountTs.setText(String.valueOf(downs));

                            if (vh.mUpBtn.isChecked()) {
                                vh.mUpBtn.toggle();

                                long ups = post.getUps();
                                if (ups != 0) {
                                    ups -= 1;
                                }

                                post.setUps(ups);
                                vh.mUpsCountTs.setText(String.valueOf(ups));
                            }

                            mOnVoteActionClickListener.onVoteAction(post.getId(), ForumFragment.DIR_DOWN);
                        } else {
                            long downs = post.getDowns();
                            if (downs != 0) {
                                downs -= 1;
                            }

                            post.setDowns(downs);
                            post.setStatus("DEFAULT");

                            vh.mDownsCountTs.setText(String.valueOf(downs));

                            mOnVoteActionClickListener.onVoteAction(post.getId(), ForumFragment.DIR_DEFAULT);
                        }

                        getAdapter().notifyItemChanged(pos, post);
                    }
                }
            });
        }

        vh.mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final ForumPost post = items.get(pos);
                    v.setTag(post.getComments());
                    mOnCommentsClickListener.onCommentClick(v, post.getId(), post.getOwnerId());
                }
            }
        });

        vh.mHasTagsTv.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mOnHashTagClickListener.onHashTagClick(matchedText.substring(1));
                }
            }
        });
    }

    private MediaObject getDetail(boolean isBigLayout, MediaDetail detail) {
        if (isBigLayout) {
            return detail.getStd();
        }
        return detail.getThumb();
    }
}
