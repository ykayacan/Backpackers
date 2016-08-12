package com.yoloo.android.backend.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;

@Entity
@Cache
@JsonPropertyOrder({"id", "username", "realname", "email", "profileImageUrl",
        "followeeCount", "followerCount", "questionCount", "createdAt", "updatedAt"})
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
     * The parentUserKey real name.
     */
    private String realname;

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
     * The provider.
     */
    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Provider provider;

    /**
     * The parentUserKey picture Url.
     */
    private Link profileImageUrl;

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

    private Account() {
    }

    private Account(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.realname = builder.realname;
        this.provider = builder.provider;
        this.profileImageUrl = builder.profileImageUrl;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public static Account.Builder builder(Key<Account> userKey) {
        return new Account.Builder(userKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Account> getKey() {
        return Key.create(Account.class, id);
    }

    @JsonProperty("id")
    public String getWebsafeKey() {
        return getKey().toWebSafeString();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email.getEmail();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getProfileImageUrl() {
        return profileImageUrl.getValue();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Link getProfileImageUrlLink() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = new Link(profileImageUrl);
    }

    public long getFolloweeCount() {
        return followeeCount;
    }

    public void setFolloweeCount(long followeeCount) {
        this.followeeCount = followeeCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(long questionCount) {
        this.questionCount = questionCount;
    }

    // Helper methods
    public boolean isValidPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public enum Provider {
        GOOGLE,
        FACEBOOK,
        YOLOO
    }

    public static final class Builder {
        private long id;
        private String username;
        private Email email;
        private String password;
        private String realname;
        private Provider provider;
        private Link profileImageUrl;

        public Builder(Key<Account> userKey) {
            this.id = userKey.getId();
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = new Email(email);
            return this;
        }

        public Builder setPassword(String password) {
            this.password = BCrypt.hashpw(password, BCrypt.gensalt());
            return this;
        }

        public Builder setRealname(String realname) {
            this.realname = realname;
            return this;
        }

        public Builder setProvider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = new Link(profileImageUrl);
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
