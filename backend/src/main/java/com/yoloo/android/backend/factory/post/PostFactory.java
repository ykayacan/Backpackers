package com.yoloo.android.backend.factory.post;

import com.yoloo.android.backend.model.feed.post.Post;

public class PostFactory {

    public static Post getPost(PostAbstractFactory factory) {
        return factory.create();
    }
}
