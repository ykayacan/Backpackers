package com.backpackers.android.backend.factory.post;

import com.backpackers.android.backend.model.feed.post.AbstractPost;

public class PostFactory {

    public static AbstractPost create(PostAbstractFactory factory) {
        return factory.create();
    }
}
