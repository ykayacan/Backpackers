package com.yoloo.android.backend.model.comment;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
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
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;

@Entity
@Cache
@JsonPropertyOrder({"id", "ownerId", "username", "profileImageUrl", "comment",
        "liked", "likes", "createdAt"})
public class Comment implements Likeable {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Parent
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> parentUser;

    @Index
    private Key<? extends Commentable> commentableKey;

    private String comment;

    private String username;

    private Link profileImageUrl;

    @Index
    private Date createdAt;

    // Extra fields

    @Ignore
    private boolean isLiked = false;

    @Ignore
    private long likes = 0L;

    private Comment() {
    }

    private Comment(Builder builder) {
        this.parentUser = builder.parentUser;
        this.commentableKey = builder.commentableKey;
        this.profileImageUrl = builder.profileImageUrl;
        this.username = builder.username;
        this.comment = builder.comment;
        this.createdAt = new Date();
    }

    public static Comment.Builder builder(Key<? extends Commentable> commentableKey,
                                          Key<Account> parentUserKey) {
        return new Comment.Builder(commentableKey, parentUserKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Comment> getKey() {
        return Key.create(parentUser.getKey(), Comment.class, id);
    }

    /**
     * Gets websafe key.
     *
     * @return the websafe key
     */
    @JsonProperty("id")
    public String getWebsafeKey() {
        return getKey().toWebSafeString();
    }

    @JsonProperty("ownerId")
    public String getParentUserKey() {
        return parentUser.getKey().toWebSafeString();
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public String getProfileImageUrl() {
        return profileImageUrl.getValue();
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

    @Override
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<? extends Likeable> getLikeableKey() {
        return getKey();
    }

    @Override
    public void setEntityLiked(boolean liked) {
        this.setLiked(liked);
    }

    @Override
    public void setEntityLikes(long count) {
        this.setLikes(count);
    }

    public static final class Builder {
        private Ref<Account> parentUser;
        private Key<? extends Commentable> commentableKey;
        private Link profileImageUrl;
        private String username;
        private String comment;

        public Builder(Key<? extends Commentable> commentableKey,
                       Key<Account> userKey) {
            this.commentableKey = commentableKey;
            this.parentUser = Ref.create(userKey);
        }

        public Builder setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = new Link(profileImageUrl);
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
