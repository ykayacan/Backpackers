package com.yoloo.android.backend.modal.like;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Feed;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Subclass;

@Subclass(index=true)
public class FeedLike extends Like {

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Feed> feedRef;

    private FeedLike() {
    }

    public FeedLike(Key<Account> accountKey, Key<Feed> feedKey) {
        super(accountKey);
        this.feedRef = Ref.create(feedKey);
    }

    public Feed getFeed() {
        return this.feedRef.get();
    }
}
