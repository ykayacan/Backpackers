package com.yoloo.android.backend.model.feed.post;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.Photo;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;
import java.util.List;

/**
 * The base class for Feed entity.
 */
public class Post implements Likeable {

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
    protected List<Photo> photos;

    /**
     * The Created at.
     */
    protected Date createdAt;

    // Methods

    Post() {
    }

    Post(Builder<?> builder) {
        this.id = builder.id;
        this.parentUserKey = builder.parentUserKey;
        this.profileImageUrl = builder.profileImageUrl;
        this.username = builder.username;
        this.content = builder.content;
        this.photos = builder.images;
        this.createdAt = new Date();
    }

    public static Builder<?> builder() {
        return new Builder<Post>() {
            @Override
            public Post build() {
                return new Post(this);
            }
        };
    }

    @JsonProperty("ownerId")
    public String getParentUserKey() {
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
    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public void addImage(Photo photo) {
        this.photos.add(photo);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public static abstract class Builder<T extends Post> {
        protected long id;
        protected Key<Account> parentUserKey;
        protected Link profileImageUrl;
        protected String username;
        protected String content;
        protected List<Photo> images;

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

        public Builder<T> setImages(List<Photo> photos) {
            this.images = photos;
            return this;
        }

        public abstract T build();
    }
}
