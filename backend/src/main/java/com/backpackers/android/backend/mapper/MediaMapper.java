package com.backpackers.android.backend.mapper;

import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.media.Media;

public class MediaMapper implements Mapper<Media, AbstractPost.PostMedia> {

    @Override
    public AbstractPost.PostMedia map(Media media) {
        return new AbstractPost.PostMedia(media.getWebsafeId(), media.getType(),
                media.getLength(), media.getUrl());
    }
}
