package com.yoloo.android.backend.model.comment;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;

@Entity
@Cache
@JsonPropertyOrder({"id", "username", "profileImageUrl", "comment",
        "liked", "likesCount", "createdAt"})
public class Comment implements Likeable {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Parent
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Question> question;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> user;

    private String comment;

    private String username;

    private String profileImageUrl;

    @Index
    private long likesCount = 0;

    @Index
    private Date createdAt;

    // Extra fields

    @Ignore
    private boolean isLiked = false;

    private Comment() {
    }

    private Comment(Builder builder) {
        this.question = builder.question;
        this.user = builder.user;
        this.profileImageUrl = builder.profileImageUrl;
        this.username = builder.username;
        this.comment = builder.comment;
        this.createdAt = new Date();
    }

    public static Comment.Builder builder(Key<Question> parentQuestionKey,
                                          Key<Account> userKey) {
        return new Comment.Builder(parentQuestionKey, userKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Comment> getKey() {
        return Key.create(question.getKey(), Comment.class, id);
    }

    @JsonProperty("id")
    public String getWebsafeKey() {
        return getKey().toWebSafeString();
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public static final class Builder {
        private Ref<Question> question;
        private Ref<Account> user;
        private String profileImageUrl;
        private String username;
        private String comment;

        public Builder(Key<Question> parentQuestionKey,
                       Key<Account> userKey) {
            this.question = Ref.create(parentQuestionKey);
            this.user = Ref.create(userKey);
        }

        public Builder setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Comment build() {
            return new Comment(this);
        }
    }
}
