package com.yoloo.android.backend.modal;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Cache
public class Feed implements Deletable {

    @Id
    @Getter
    private Long id;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> accountRef;

    @Getter
    private String username;

    // TODO: 8.06.2016 implement runtime version.
    @Getter
    private int type;

    @Getter
    private String title;

    @Getter
    private String content;

    @Index(IfNotDefault.class)
    @Getter
    private long commentsCount = 0;

    @Index(IfNotDefault.class)
    @Getter
    private long likesCount = 0;

    // workaround
    @Ignore
    @Getter
    @Setter
    private List<String> hashtags;

    // workaround
    @Ignore
    @Getter
    @Setter
    private List<String> locations;

    @Ignore
    @Setter
    @Getter
    private boolean isLiked = false;

    @Ignore
    @Getter
    private List<Photo> images;

    @Index
    @Getter
    private Date createdAt;

    @Index(IfNotDefault.class)
    @Getter
    @Setter
    private Date updatedAt;

    private Feed() {
    }

    private Feed(Ref<Account> accountRef, int type, String title, String content,
                 List<String> hashtags, List<String> locations) {
        this.accountRef = accountRef;
        this.username = this.accountRef.get().getUsername();
        this.type = type;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
        this.locations = locations;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Account getAccount() {
        return this.accountRef.get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Feed> getKey() {
        return Key.create(Feed.class, id);
    }

    public void increaseLikeCounter() {
        this.likesCount++;
    }

    public void decreaseLikeCounter() {
        if (this.likesCount >= 0) {
            this.likesCount--;
        }
    }

    public void increaseCommentCounter() {
        this.commentsCount++;
    }

    public void decreaseCommentCounter() {
        if (this.commentsCount >= 0) {
            this.commentsCount--;
        }
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Override
    public Key<Account> getAccountKey() {
        return this.accountRef.key();
    }

    public static final class Builder {
        private Ref<Account> accountRef;
        private int type;
        private String title;
        private String content;
        private List<String> hashtags;
        private List<String> locations;

        public Builder setAccount(Key<Account> accountKey) {
            this.accountRef = Ref.create(accountKey);
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder hashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        public Builder locations(List<String> locations) {
            this.locations = locations;
            return this;
        }

        public Feed build() {
            return new Feed(accountRef, type, title, content, hashtags, locations);
        }
    }
}
