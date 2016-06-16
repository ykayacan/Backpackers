package com.yoloo.android.backend.modal.like;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Subclass;

@Subclass(index = true)
public class CommentLike extends Like {

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Comment> commentRef;

    private CommentLike() {
    }

    public CommentLike(Key<Account> accountKey, Key<Comment> commentKey) {
        super(accountKey);
        this.commentRef = Ref.create(commentKey);
    }

    public Comment getComment() {
        return this.commentRef.get();
    }
}
