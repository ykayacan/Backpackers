package com.yoloo.android.backend.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.GeoPt;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.yoloo.android.backend.model.feed.post.Post;

@Entity
@Cache
public class Location {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<? extends Post> postKey;

    @Index
    private String name;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private GeoPt geoPt;

    Location() {
    }

    public static Location.Builder builder() {
        return new Location.Builder();
    }

    private Location(Builder builder) {
        this.postKey = builder.postKey;
        this.name = builder.name;
        this.geoPt = builder.geoPt;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Location> getKey() {
        return Key.create(Location.class, id);
    }

    public void setGeoPt(GeoPt geoPt) {
        this.geoPt = geoPt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitude() {
        return this.geoPt.getLatitude();
    }

    public float getLongitude() {
        return this.geoPt.getLongitude();
    }

    public static final class Builder {
        private Key<? extends Post> postKey;
        private String name;
        private GeoPt geoPt;

        public Builder setPostKey(Key<? extends Post> postKey) {
            this.postKey = postKey;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setGeoPt(GeoPt geoPt) {
            this.geoPt = geoPt;
            return this;
        }

        public Location build() {
            return new Location(this);
        }
    }
}
