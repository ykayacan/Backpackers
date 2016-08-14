package com.yoloo.android.backend.model.media;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;
import java.util.UUID;

@Entity
@Cache
public class Media {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    private String fileName = UUID.randomUUID().toString();

    private String contentType;

    private long size;

    private Date createdAt;

    private Media() {
    }

    public static Media.Builder builder(Key<Account> parentUserKey) {
        return new Media.Builder(parentUserKey);
    }

    private Media(Builder builder) {
        this.parentUserKey = builder.parentUserKey;
        this.contentType = builder.contentType;
        this.size = builder.size;
        this.createdAt = new Date();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Media> getKey() {
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

    public static final class Builder {
        private Key<Account> parentUserKey;
        private String contentType;
        private long size;

        public Builder(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Media build() {
            return new Media(this);
        }
    }
}
