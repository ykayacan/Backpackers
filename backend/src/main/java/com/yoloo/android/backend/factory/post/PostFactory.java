package com.yoloo.android.backend.factory.post;

import com.yoloo.android.backend.model.feed.post.AbstractPost;

public class PostFactory {

    public static AbstractPost getPost(PostAbstractFactory factory) {
        return factory.create();
    }
}
