package com.yoloo.android.backend.modal;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.Date;

import lombok.Getter;

@Entity
@Cache
public class Comment implements Deletable {

    @Id
    @Getter
    private Long id;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Feed> feedRef;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> accountRef;

    @Getter
    private Link accountPhotoUrl;

    @Getter
    private String username;

    @Getter
    private String comment;

    @Index(IfNotDefault.class)
    @Getter
    private long likesCount = 0;

    @Index
    @Getter
    private Date createdAt;

    private Comment() {
    }

    private Comment(Ref<Feed> feedRef, Ref<Account> accountRef,
                    Link accountPhotoUrl, String username,
                    String comment) {
        this.feedRef = feedRef;
        this.accountRef = accountRef;
        this.accountPhotoUrl = accountPhotoUrl;
        this.username = username;
        this.comment = comment;
        this.createdAt = new Date();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Feed getFeed() {
        return this.feedRef.get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Account getAccount() {
        return this.accountRef.get();
    }

    public void increaseLikeCounter() {
        this.likesCount++;
    }

    public void decreaseLikeCounter() {
        if (this.likesCount >= 0) {
            this.likesCount--;
        }
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Override
    public Key<Account> getAccountKey() {
        return this.accountRef.key();
    }

    public static final class Builder {
        private Ref<Feed> feedRef;
        private Ref<Account> accountRef;
        private Link accountPhotoUrl;
        private String username;
        private String comment;

        public Builder setFeed(Key<Feed> feedKey) {
            this.feedRef = Ref.create(feedKey);
            return this;
        }

        public Builder setAccount(Key<Account> accountKey) {
            this.accountRef = Ref.create(accountKey);
            return this;
        }

        public Builder setAccountPhotoUrl(String url) {
            this.accountPhotoUrl = new Link(url);
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
            return new Comment(feedRef, accountRef, accountPhotoUrl, username, comment);
        }
    }
}
