package com.yoloo.android.backend.model.question;

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
@Cache
public class QuestionCounter {

    @Id
    private Long id;

    @Parent
    @Load
    private Ref<Question> question;

    @Index(IfNotDefault.class)
    private long likesCount = 0;

    @Index(IfNotDefault.class)
    private long commentsCount = 0;

    private QuestionCounter() {
    }

    private QuestionCounter(Key<Question> parentQuestionKey) {
        this.question = Ref.create(parentQuestionKey);
    }

    public static QuestionCounter newInstance(Key<Question> parentQuestionKey) {
        return new QuestionCounter(parentQuestionKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<QuestionCounter> getKey() {
        return Key.create(question.getKey(), QuestionCounter.class, id);
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }
}
