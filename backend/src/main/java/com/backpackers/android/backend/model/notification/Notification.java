package com.backpackers.android.backend.model.notification;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.backpackers.android.backend.model.user.Account;

import java.util.Date;

@Entity
@Cache
@JsonPropertyOrder({"id", "senderId", "receiverId", "profileImageUrl", "username", "action", "createdAt"})
public class Notification {

    @Id
    private Long id;

    @Parent
    private Key<Account> receiverAccountKey;

    private Key<Account> senderAccountKey;

    private String username;

    private Link profileImageUrl;

    private String content;

    private String locale;

    private Action action;

    private String websafePostId;

    private String websafeCommentId;

    @Index
    private Date createdAt;

    private Notification() {
    }

    private Notification(Builder builder) {
        this.senderAccountKey = builder.senderAccountKey;
        this.receiverAccountKey = builder.receiverAccountKey;
        this.username = builder.username;
        this.profileImageUrl = builder.profileImageUrl;
        this.content = builder.content;
        this.locale = builder.locale;
        this.action = builder.action;
        this.websafePostId = builder.websafePostId;
        this.websafeCommentId = builder.websafeCommentId;
        this.createdAt = new Date();
    }

    public static Builder builder(Key<Account> senderAccountKey,
                                  Key<Account> receiverAccountKey) {
        return new Builder(senderAccountKey, receiverAccountKey);
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Notification> getKey() {
        return Key.create(senderAccountKey, Notification.class, id);
    }

    /**
     * Gets websafe id.
     *
     * @return the websafe id
     */
    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Account> getSenderAccountKey() {
        return senderAccountKey;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Account> getReceiverAccountKey() {
        return receiverAccountKey;
    }

    public String getSenderId() {
        return senderAccountKey.toWebSafeString();
    }

    public String getReceiverId() {
        return receiverAccountKey.toWebSafeString();
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets profile image url.
     *
     * @return the profile image url
     */
    public String getProfileImageUrl() {
        return profileImageUrl.getValue();
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets locale.
     *
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets action.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    public String getWebsafePostId() {
        return websafePostId;
    }

    public String getWebsafeCommentId() {
        return websafeCommentId;
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * The enum Action.
     */
    public enum Action {
        /**
         * Follow action.
         */
        FOLLOW, /**
         * Comment action.
         */
        COMMENT, /**
         * Ask action.
         */
        ASK, /**
         * Mention action.
         */
        MENTION
    }

    public static final class Builder {
        private Key<Account> senderAccountKey;
        private Key<Account> receiverAccountKey;
        private String username;
        private Link profileImageUrl;
        private String content;
        private String locale;
        private Action action;
        private String websafePostId;
        private String websafeCommentId;

        /**
         * Instantiates a new Builder.
         *
         * @param senderAccountKey the account key
         */
        public Builder(Key<Account> senderAccountKey,
                       Key<Account> receiverAccountKey) {
            this.senderAccountKey = senderAccountKey;
            this.receiverAccountKey = receiverAccountKey;
        }

        /**
         * Sets username.
         *
         * @param username the username
         * @return the username
         */
        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets profile image url.
         *
         * @param profileImageUrl the profile image url
         * @return the profile image url
         */
        public Builder setProfileImageUrl(Link profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        /**
         * Sets content.
         *
         * @param content the content
         * @return the content
         */
        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Sets locale.
         *
         * @param locale the locale
         * @return the locale
         */
        public Builder setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Sets action.
         *
         * @param action the action
         * @return the action
         */
        public Builder setAction(Action action) {
            this.action = action;
            return this;
        }

        public Builder setPostId(String websafePostId) {
            this.websafePostId = websafePostId;
            return this;
        }

        public Builder setCommentId(String websafeCommentId) {
            this.websafeCommentId = websafeCommentId;
            return this;
        }

        /**
         * Build message.
         *
         * @return the message
         */
        public Notification build() {
            return new Notification(this);
        }
    }
}
