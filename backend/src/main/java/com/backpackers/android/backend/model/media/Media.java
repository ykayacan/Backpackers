package com.backpackers.android.backend.model.media;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.backpackers.android.backend.model.media.mediadetail.MediaDetail;
import com.backpackers.android.backend.util.MediaUtil;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.MimeUtil;

@Entity
@JsonPropertyOrder({"id", "mime", "length"})
public class Media {

    @Id
    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Parent
    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Account> parentUserKey;

    @Index
    private String websafePostId;

    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Metadata meta;

    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Link url;

    // Extra params.

    @Ignore
    private MediaDetail mediaDetail;

    private Media() {
    }

    private Media(Builder builder) {
        this.parentUserKey = builder.parentUserKey;
        this.meta = builder.meta;
        this.url = MediaUtil.getServingUrl(meta);
    }

    /**
     * Builder media . builder.
     *
     * @param parentUserKey the parent user key
     * @return the media . builder
     */
    public static Media.Builder builder(Key<Account> parentUserKey) {
        return new Media.Builder(parentUserKey);
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Media> getKey() {
        return Key.create(parentUserKey, Media.class, id);
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
    @JsonIgnore
    public String getWebsafeParentUserId() {
        return parentUserKey.toWebSafeString();
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    @JsonProperty("mime")
    public String getType() {
        return meta.getMimeType();
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public long getLength() {
        return meta.getLength();
    }

    @JsonIgnore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Link getUrl() {
        return url;
    }

    public void setWebsafePostId(String websafePostId) {
        this.websafePostId = websafePostId;
    }

    @JsonIgnore
    public Metadata getMeta() {
        return meta;
    }

    /**
     * Gets media detail.
     *
     * @return the media detail
     */
    @JsonProperty("detail")
    public MediaDetail getMediaDetail() {
        final String mimeType = meta.getMimeType();
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

    public static final class Builder {
        private Key<Account> parentUserKey;
        private Metadata meta;

        /**
         * Instantiates a new Builder.
         *
         * @param parentUserKey the parent user key
         */
        public Builder(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
        }

        /**
         * Sets meta.
         *
         * @param meta the meta
         * @return the meta
         */
        public Builder setMeta(Metadata meta) {
            this.meta = meta;
            return this;
        }

        /**
         * Build media.
         *
         * @return the media
         */
        public Media build() {
            return new Media(this);
        }
    }
}
