package com.yoloo.android.backend.counter.comment;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.comment.Comment;

public final class CommentLikeCounter implements Count {

    private final Comment comment;

    public CommentLikeCounter(Comment comment) {
        this.comment = comment;
    }

    @Override
    public void increase() {
        comment.setLikesCount(comment.getLikesCount() + 1);
    }

    @Override
    public void decrease() {
        if (comment.getLikesCount() >= 0) {
            comment.setLikesCount(comment.getLikesCount() - 1);
        }
    }
}
