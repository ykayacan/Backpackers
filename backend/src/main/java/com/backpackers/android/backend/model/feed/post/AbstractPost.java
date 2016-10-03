package com.backpackers.android.backend.model.feed.post;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.backpackers.android.backend.mapper.MediaMapper;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.media.MediaObject;
import com.backpackers.android.backend.model.media.mediadetail.MediaDetail;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.MimeUtil;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
     * The Medias.
     */
    protected List<PostMedia> medias;

    /**
     * The Created at.
     */
    @Index
    protected Date createdAt;

    // Extra fields

    @Ignore
    private String type;

    // Methods

    /**
     * Instantiates a new Abstract post.
     */
    AbstractPost() {
    }

    /**
     * Instantiates a new Abstract post.
     *
     * @param builder the builder
     */
    AbstractPost(Builder<?> builder) {
        this.id = builder.id;
        this.parentUserKey = builder.parentUserKey;
        this.profileImageUrl = builder.profileImageUrl;
        this.username = builder.username;
        this.content = builder.content;
        this.medias = builder.medias;
        this.createdAt = new Date();
    }

    /**
     * Builder builder.
     *
     * @return the builder
     */
    public static Builder<?> builder() {
        return new Builder<AbstractPost>() {
            @Override
            public AbstractPost build() {
                return new AbstractPost(this);
            }
        };
    }

    /**
     * Gets key.
     *
     * @return the key
     */
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

    /**
     * Gets websafe parent user id.
     *
     * @return the websafe parent user id
     */
    @JsonProperty("ownerId")
    public String getWebsafeOwnerId() {
        return parentUserKey.toWebSafeString();
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
    public void setProfileImageUrl(Link profileImageUrl) {
        this.profileImageUrl = profileImageUrl == null ? null : profileImageUrl;
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
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets medias.
     *
     * @return the medias
     */
    @JsonProperty("medias")
    public List<PostMedia> getMedias() {
        return medias;
    }

    /**
     * Sets medias.
     *
     * @param medias the medias
     */
    public void setMedias(Collection<Media> medias) {
        final MediaMapper mapper = new MediaMapper();
        for (Media media : medias) {
            this.medias.add(mapper.map(media));
        }
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
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        type = getClass().getSimpleName().replace("Post", "").toLowerCase();
        return type;
    }

    /**
     * The type Builder.
     *
     * @param <T> the type parameter
     */
    public static abstract class Builder<T extends AbstractPost> {
        /**
         * The Id.
         */
        protected long id;

        /**
         * The Parent user key.
         */
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
         * The Medias.
         */
        protected List<PostMedia> medias = new ArrayList<>(3);

        /**
         * Sets key.
         *
         * @param key the key
         * @return the key
         */
        public Builder<T> setKey(Key<?> key) {
            this.id = key.getId();
            return this;
        }

        /**
         * Sets parent user key.
         *
         * @param parentUserKey the parent user key
         * @return the parent user key
         */
        public Builder<T> setParentUserKey(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
            return this;
        }

        /**
         * Sets profile image url.
         *
         * @param profileImageUrl the profile image url
         * @return the profile image url
         */
        public Builder<T> setProfileImageUrl(Link profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        /**
         * Sets username.
         *
         * @param username the username
         * @return the username
         */
        public Builder<T> setUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets content.
         *
         * @param content the content
         * @return the content
         */
        public Builder<T> setContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Sets medias.
         *
         * @param medias the medias
         * @return the medias
         */
        public Builder<T> setMedias(Collection<Media> medias) {
            if (medias == null) {
                this.medias = Collections.emptyList();
            } else {
                final MediaMapper mapper = new MediaMapper();
                for (Media media : medias) {
                    this.medias.add(mapper.map(media));
                }
            }
            return this;
        }

        /**
         * Build t.
         *
         * @return the t
         */
        public abstract T build();
    }

    /**
     * Inner class for medias. The original {@link Media} class is too heavy to include in
     * {@link AbstractPost}.
     */
    @JsonPropertyOrder({"id", "mime", "length"})
    public static class PostMedia {

        @JsonProperty("id")
        private String websafeMediaId;

        @JsonIgnore
        @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
        private Link url;

        private String mimeType;
        private long length;

        // Extra params.
        @Ignore
        private MediaDetail mediaDetail;

        private PostMedia() {
        }

        public PostMedia(String websafeMediaId, String mimeType, long length, Link url) {
            this.websafeMediaId = websafeMediaId;
            this.mimeType = mimeType;
            this.length = length;
            this.url = url;
        }

        @JsonProperty("id")
        public String getWebsafeMediaId() {
            return websafeMediaId;
        }

        @JsonProperty("mime")
        public String getMimeType() {
            return mimeType;
        }

        public long getLength() {
            return length;
        }

        @JsonProperty("detail")
        public MediaDetail getMediaDetail() {
            if (MimeUtil.isPhoto(mimeType)) {
                return MediaDetail.builder()
                        .setLowImage(new MediaObject(url, mimeType, MediaObject.Resolution.LOW))
                        .setStandardImage(new MediaObject(url, mimeType, MediaObject.Resolution.STANDARD))
                        .setThumbImage(new MediaObject(url, mimeType, MediaObject.Resolution.THUMB))
                        .build();
            } else if (MimeUtil.isVideo(mimeType)) {
                return MediaDetail.builder()
                        .setStandardVideo(new MediaObject(url, mimeType, MediaObject.Resolution.STANDARD))
                        .build();
            } else {
                throw new IllegalArgumentException("Unsupported mime type.");
            }
        }
    }
}
