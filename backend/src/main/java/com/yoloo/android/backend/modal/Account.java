package com.yoloo.android.backend.modal;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Entity
@Cache
public class Account {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    @Getter
    private Long id;

    /**
     * The user real name.
     */
    @Index
    @Getter
    @Setter
    private String realname;

    /**
     * The user user name.
     */
    @Index
    @Getter
    @Setter
    private String username;

    /**
     * The user email.
     */
    @Index
    @Getter
    @Setter
    private Email email;

    /**
     * The user picture Url.
     */
    @Getter
    @Setter
    private Link pictureUrl;

    /**
     * The user locale.
     */
    @Getter
    @Setter
    private String locale;

    /**
     * The user access token.
     */
    @Index
    @Getter
    @Setter
    private String accessToken;

    /**
     * The user follower count.
     */
    @Index(IfNotDefault.class)
    @Getter
    private long followeeCount = 0;

    /**
     * The user following count.
     */
    @Index(IfNotDefault.class)
    @Getter
    private long followerCount = 0;

    /**
     * The user creation date.
     */
    @Index
    @Getter
    private Date createdAt;

    /**
     * The user update date.
     */
    @Index(IfNotDefault.class)
    @Getter
    @Setter
    private Date updatedAt;

    private Account() {
    }

    private Account(String realname, String username, Email email,
                    Link pictureUrl, String locale, String accessToken) {
        this.realname = realname;
        this.username = username;
        this.email = email;
        this.pictureUrl = pictureUrl;
        this.locale = locale;
        this.accessToken = accessToken;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public void increaseFolloweeCount() {
        this.followeeCount++;
    }

    public void increaseFollowerCount() {
        this.followerCount++;
    }

    public void decreaseFolloweeCount() {
        if (this.followeeCount >= 0) {
            this.followeeCount--;
        }
    }

    public void decreaseFollowerCount() {
        if (this.followerCount >= 0) {
            this.followerCount--;
        }
    }

    public static final class Builder {
        private String realname;
        private String username;
        private Email email;
        private Link pictureUrl;
        private String locale;
        private String accessToken;

        public Builder setRealname(String realname) {
            this.realname = realname;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = new Email(email);
            return this;
        }

        public Builder setPictureUrl(String pictureUrl) {
            this.pictureUrl = new Link(pictureUrl);
            return this;
        }

        public Builder setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder setAccessToken(String token) {
            this.accessToken = token;
            return this;
        }

        public Account build() {
            return new Account(realname, username, email, pictureUrl, locale, accessToken);
        }
    }
}
