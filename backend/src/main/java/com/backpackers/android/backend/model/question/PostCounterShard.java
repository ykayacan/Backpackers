package com.backpackers.android.backend.model.question;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache(expirationSeconds = 60)
public class PostCounterShard {

    @Id
    private Long id;

    @Parent
    @Load
    private Ref<Question> question;

    @Index(IfNotDefault.class)
    private long likesCount = 0;

    @Index(IfNotDefault.class)
    private long commentsCount = 0;

    private PostCounterShard() {
    }

    private PostCounterShard(Key<Question> parentQuestionKey) {
        this.question = Ref.create(parentQuestionKey);
    }

    public static PostCounterShard newInstance(Key<Question> parentQuestionKey) {
        return new PostCounterShard(parentQuestionKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<PostCounterShard> getKey() {
        return Key.create(question.getKey(), PostCounterShard.class, id);
    }

    public long getLikeCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getCommentCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }
}
