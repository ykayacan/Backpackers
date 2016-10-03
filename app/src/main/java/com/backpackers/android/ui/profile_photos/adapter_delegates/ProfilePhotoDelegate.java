package com.backpackers.android.ui.profile_photos.adapter_delegates;

import com.backpackers.android.backend.modal.yolooApi.model.Media;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.recyclerview.AbsAdapterDelegate;
import com.backpackers.android.ui.widget.SquaredImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfilePhotoDelegate extends AbsAdapterDelegate<Media, ProfilePhotoDelegate.PhotoViewHolder> {

    private Context mContext;
    private final OnItemClickListener<Media> mOnItemClickListener;

    public ProfilePhotoDelegate(OnItemClickListener<Media> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected int getLayoutResId() {
        return com.backpackers.android.R.layout.item_photo_grid;
    }

    @Override
    protected boolean isForViewType(@NonNull Media item, int position) {
        return true;
    }

    @NonNull
    @Override
    protected PhotoViewHolder onCreateViewHolder(ViewGroup parent, View view, List<Media> items) {
        final PhotoViewHolder vh = new PhotoViewHolder(view);
        mContext = vh.itemView.getContext();
        setupCommonClickableViews(vh, items);
        return vh;
    }

    @Override
    protected void onBindViewHolder(Media item, PhotoViewHolder holder, int position) {
        Glide.with(mContext)
                .load(item.getDetail().getThumb().getUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mGridIv);
    }

    private void setupCommonClickableViews(final PhotoViewHolder vh,
                                           final List<Media> items) {
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Media media = items.get(pos);
                    mOnItemClickListener.onItemClick(v, media);
                    /*mOnReadMoreClickListener.onReadMoreClick(v, pos, media);*/
                }
            }
        });
    }

    static final class PhotoViewHolder extends RecyclerView.ViewHolder {

        @BindView(com.backpackers.android.R.id.image_grid)
        SquaredImageView mGridIv;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
