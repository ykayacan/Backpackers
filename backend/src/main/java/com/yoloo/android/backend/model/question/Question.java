package com.yoloo.android.backend.model.question;

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
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cache
@JsonPropertyOrder({"id", "title", "content", "hashtags", "liked", "location",
        "likesCount", "commentsCount", "createdAt", "updatedAt"})
public class Question {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long id;

    @Parent
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> user;

    private String title;

    private String content;

    @Index
    private Set<String> hashtags;

    @Index
    private Date createdAt;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    @Ignore
    private String username;

    @Ignore
    private String profileImageUrl;

    @Ignore
    private long likesCount;

    @Ignore
    private long commentsCount;

    @Ignore
    private Location location;

    @Ignore
    private boolean isLiked = false;

    // Methods

    private Question() {
    }

    private Question(Builder builder) {
        this.id = builder.questionKey.getId();
        this.user = builder.user;
        this.title = builder.title;
        this.content = builder.content;
        this.hashtags = builder.hashtags;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public static Question.Builder builder(Key<Question> questionKey, Key<Account> userKey) {
        return new Question.Builder(questionKey, userKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Account getParentUser() {
        return this.user.get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Question> getKey() {
        return Key.create(user.getKey(), Question.class, id);
    }

    @JsonProperty("id")
    public String getWebsafeKey() {
        return getKey().toWebSafeString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public static final class Builder {
        private Key<Question> questionKey;
        private Ref<Account> user;
        private String title;
        private String content;
        private Set<String> hashtags = new HashSet<>();

        public Builder(Key<Question> questionKey, Key<Account> userKey) {
            this.questionKey = questionKey;
            this.user = Ref.create(userKey);
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setHashtag(Collection<String> hashtags) {
            this.hashtags.addAll(hashtags);
            return this;
        }

        public Question build() {
            return new Question(this);
        }
    }
}
