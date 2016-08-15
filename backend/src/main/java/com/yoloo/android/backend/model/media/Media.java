package com.yoloo.android.backend.model.media;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.user.Account;

@Entity
@JsonPropertyOrder({"id", "type", "size"})
public class Media {

    @Id
    @JsonIgnore
    private Long id;

    @Parent
    @JsonIgnore
    private Key<Account> parentUserKey;

    @JsonIgnore
    private GcsFileMetadata meta;

    // Extra params.

    @JsonProperty("detail")
    @Ignore
    private Resolution resolution;

    private Media() {
    }

    public static Media.Builder builder(Key<Account> parentUserKey) {
        return new Media.Builder(parentUserKey);
    }

    private Media(Builder builder) {
        this.parentUserKey = builder.parentUserKey;
        this.meta = builder.meta;
        this.resolution = builder.data;
    }

    @JsonIgnore
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

    @JsonIgnore
    public String getWebsafeParentUserId() {
        return parentUserKey.toWebSafeString();
    }

    @JsonProperty("type")
    public String getType() {
        return this.meta.getOptions().getMimeType();
    }

    public long getSize() {
        return this.meta.getLength();
    }

    public static final class Builder {
        private Key<Account> parentUserKey;
        private GcsFileMetadata meta;
        private Resolution data;

        public Builder(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
        }

        public Builder setMeta(GcsFileMetadata meta) {
            this.meta = meta;
            return this;
        }

        public Builder setData(Resolution data) {
            this.data = data;
            return this;
        }

        public Media build() {
            return new Media(this);
        }
    }
}
