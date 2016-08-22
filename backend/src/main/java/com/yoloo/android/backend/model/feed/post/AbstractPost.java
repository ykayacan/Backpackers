package com.yoloo.android.backend.model.feed.post;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.Image;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;
import java.util.List;

/**
 * The base class for Feed entity.
 */
public class AbstractPost {

    /**
     * The Id.
     */
    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    protected long id;

    /**
     * The Parent user key.
     */
    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    protected Key<Account> parentUserKey;

    /**
     * The Profile image url.
     */
    protected Link profileImageUrl;

    /**
     * The Username.
     */
    protected String username;

    /**
     * The Content.
     */
    protected String content;

    /**
     * The Photos.
     */
    protected List<Image> mImages;

    /**
     * The Created at.
     */
    @Index
    protected Date createdAt;

    // Extra fields

    @Ignore
    private String type;

    // Methods

    AbstractPost() {
    }

    AbstractPost(Builder<?> builder) {
        this.id = builder.id;
        this.parentUserKey = builder.parentUserKey;
        this.profileImageUrl = builder.profileImageUrl;
        this.username = builder.username;
        this.content = builder.content;
        this.mImages = builder.images;
        this.createdAt = new Date();
    }

    public static Builder<?> builder() {
        return new Builder<AbstractPost>() {
            @Override
            public AbstractPost build() {
                return new AbstractPost(this);
            }
        };
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<? extends AbstractPost> getKey() {
        return Key.create(parentUserKey, this.getClass(), id);
    }

    /**
     * Gets websafe key.
     *
     * @return the websafe key
     */
    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }

    @JsonProperty("ownerId")
    public String getWebsafeParentUserId() {
        return parentUserKey.toWebSafeString();
    }

    public String getProfileImageUrl() {
        return profileImageUrl.getValue();
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl =
                Strings.isNullOrEmpty(profileImageUrl) ? null : new Link(profileImageUrl);
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JsonProperty("images")
    public List<Image> getImages() {
        return mImages;
    }

    public void setImages(List<Image> images) {
        this.mImages = images;
    }

    public void addImage(Image image) {
        this.mImages.add(image);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getType() {
        type = getClass().getSimpleName().replace("Post", "").toLowerCase();
        return type;
    }

    public static abstract class Builder<T extends AbstractPost> {
        protected long id;
        protected Key<Account> parentUserKey;
        protected Link profileImageUrl;
        protected String username;
        protected String content;
        protected List<Image> images;

        public Builder<T> setKey(Key<?> key) {
            this.id = key.getId();
            return this;
        }

        public Builder<T> setParentUserKey(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
            return this;
        }

        public Builder<T> setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl =
                    Strings.isNullOrEmpty(profileImageUrl) ? null : new Link(profileImageUrl);
            return this;
        }

        public Builder<T> setProfileImageUrl(Link profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Builder<T> setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder<T> setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder<T> setImages(List<Image> images) {
            this.images = images;
            return this;
        }

        public abstract T build();
    }
}
