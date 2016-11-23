package com.backpackers.android.backend.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.backpackers.android.backend.algorithm.bcrypt.BCrypt;
import com.backpackers.android.backend.badge.Badge;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Cache
@JsonPropertyOrder({"id", "username", "email", "profileImageUrl", "locale", "followees", "followers",
        "questions", "createdAt", "updatedAt"})
public class Account {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long id;

    /**
     * The parentUserKey parentUserKey name.
     */
    @Index
    private String username;

    /**
     * The parentUserKey email.
     */
    @Index
    private Email email;

    /**
     * The parentUserKey password.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String password;

    /**
     * The real name of the user.
     */
    private String realName;

    /**
     * The provider.
     */
    @Index
    private Provider provider;

    /**
     * The parentUserKey picture Url.
     */
    private Link profileImageUrl;

    private String locale;

    private List<Badge> badges = new ArrayList<>();

    /**
     * The parentUserKey creation date.
     */
    @Index
    private Date createdAt;

    /**
     * The parentUserKey update date.
     */
    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    @Ignore
    private long followeeCount;

    @Ignore
    private long followerCount;

    @Ignore
    private long questionCount;

    @Ignore
    private boolean isFollowing;

    private Account() {
    }

    private Account(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.provider = builder.provider;
        this.realName = builder.realName;
        this.profileImageUrl = builder.profileImageUrl;
        this.locale = builder.locale;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * Builder account . builder.
     *
     * @param userKey the user key
     * @return the account . builder
     */
    public static Account.Builder builder(Key<Account> userKey) {
        return new Account.Builder(userKey);
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Account> getKey() {
        return Key.create(Account.class, id);
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

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email.getEmail();
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
     * Gets updated at.
     *
     * @return the updated at
     */
    public Date getUpdatedAt() {
        return updatedAt;
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
     * Sets profile image url.
     *
     * @param profileImageUrl the profile image url
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = new Link(profileImageUrl);
    }

    /**
     * Gets profile image url link.
     *
     * @return the profile image url link
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Link getProfileImageUrlLink() {
        return profileImageUrl;
    }

    /**
     * Gets locale.
     *
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    public String getRealName() {
        return realName;
    }

    /**
     * Sets locale.
     *
     * @param locale the locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Gets badges.
     *
     * @return the badges
     */
    public List<Badge> getBadges() {
        return badges;
    }

    /**
     * Sets badges.
     *
     * @param badge the badges
     */
    public void addBadge(Badge badge) {
        this.badges.add(badge);
    }

    public void addBadges(List<Badge> badges) {
        this.badges.addAll(badges);
    }

    /**
     * Gets provider.
     *
     * @return the provider
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Gets followee count.
     *
     * @return the followee count
     */
    @JsonProperty("followees")
    public long getFolloweeCount() {
        return followeeCount;
    }

    /**
     * Sets followee count.
     *
     * @param followeeCount the followee count
     */
    public void setFolloweeCount(long followeeCount) {
        this.followeeCount = followeeCount;
    }

    /**
     * Gets follower count.
     *
     * @return the follower count
     */
    @JsonProperty("followers")
    public long getFollowerCount() {
        return followerCount;
    }

    /**
     * Sets follower count.
     *
     * @param followerCount the follower count
     */
    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    /**
     * Gets question count.
     *
     * @return the question count
     */
    @JsonProperty("questions")
    public long getQuestionCount() {
        return questionCount;
    }

    /**
     * Sets question count.
     *
     * @param questionCount the question count
     */
    public void setQuestionCount(long questionCount) {
        this.questionCount = questionCount;
    }

    /**
     * Is following boolean.
     *
     * @return the boolean
     */
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * Sets following.
     *
     * @param following the following
     */
    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    // Helper methods

    /**
     * Is valid password boolean.
     *
     * @param password the password
     * @return the boolean
     */
    public boolean isValidPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    /**
     * The enum Provider.
     */
    public enum Provider {
        /**
         * Google provider.
         */
        GOOGLE,
        /**
         * Facebook provider.
         */
        FACEBOOK,
        /**
         * Yoloo provider.
         */
        YOLOO
    }

    /**
     * The type Builder.
     */
    public static final class Builder {
        private long id;
        private String username;
        private Email email;
        private String password;
        private String realName;
        private Provider provider;
        private String locale;
        private Link profileImageUrl;

        /**
         * Instantiates a new Builder.
         *
         * @param userKey the user key
         */
        public Builder(Key<Account> userKey) {
            this.id = userKey.getId();
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
         * Sets email.
         *
         * @param email the email
         * @return the email
         */
        public Builder setEmail(String email) {
            this.email = new Email(email);
            return this;
        }

        /**
         * Sets password.
         *
         * @param password the password
         * @return the password
         */
        public Builder setPassword(String password) {
            this.password = BCrypt.hashpw(password, BCrypt.gensalt());
            return this;
        }

        /**
         * Sets provider.
         *
         * @param provider the provider
         * @return the provider
         */
        public Builder setProvider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder setRealName(String realName) {
            this.realName = realName;
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
         * Sets profile image url.
         *
         * @param profileImageUrl the profile image url
         * @return the profile image url
         */
        public Builder setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = new Link(profileImageUrl);
            return this;
        }

        /**
         * Build account.
         *
         * @return the account
         */
        public Account build() {
            return new Account(this);
        }
    }
}