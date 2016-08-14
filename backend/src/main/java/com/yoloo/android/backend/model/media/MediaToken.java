package com.yoloo.android.backend.model.media;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class MediaToken {

    @Id
    private Long id;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<MediaToken> getKey() {
        return Key.create(MediaToken.class, id);
    }

    @JsonProperty("token")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }
}
