package com.yoloo.android.backend.modal.location;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.GeoPt;

import com.yoloo.android.backend.modal.Feed;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import lombok.Getter;
import lombok.Setter;

@Entity
@Cache
public class Location {

    @Id
    @Getter
    private Long id;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Feed> feedRef;

    @Index
    @Getter
    @Setter
    private String name;

    @Index
    @Getter
    private GeoPt geoPt;

    public void setFeedRef(final Key<Feed> feedKey) {
        this.feedRef = Ref.create(feedKey);
    }

    public void setGeoPt(float latitude, float longitude) {
        this.geoPt = new GeoPt(latitude, longitude);
    }

    public Feed getFeed() {
        return this.feedRef.get();
    }
}
