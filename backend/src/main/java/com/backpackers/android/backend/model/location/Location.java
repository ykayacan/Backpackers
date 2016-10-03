package com.backpackers.android.backend.model.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.GeoPt;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.backpackers.android.backend.model.feed.post.AbstractPost;

@Entity
@Cache
public class Location {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<? extends AbstractPost> postKey;

    @Index
    private String name;

    /**
     * The address of the place.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String address;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private GeoPt location;

    Location() {
    }

    private Location(Builder builder) {
        this.postKey = builder.postKey;
        this.name = builder.name;
        this.location = builder.geoPt;
    }

    public static Location.Builder builder() {
        return new Location.Builder();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Location> getKey() {
        return Key.create(Location.class, id);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getLatitude() {
        return this.location.getLatitude();
    }

    public float getLongitude() {
        return this.location.getLongitude();
    }

    public static final class Builder {
        private Key<? extends AbstractPost> postKey;
        private String name;
        private GeoPt geoPt;

        public Builder setPostKey(Key<? extends AbstractPost> postKey) {
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
