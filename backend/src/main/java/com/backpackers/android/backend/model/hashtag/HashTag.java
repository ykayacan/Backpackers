package com.backpackers.android.backend.model.hashtag;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Objects;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Cache
public class HashTag {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    @Index
    private String hashTag;

    private HashTag() {
    }

    public HashTag(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getHashTag() {
        return hashTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashTag)) return false;
        HashTag hashTag1 = (HashTag) o;
        return Objects.equal(hashTag, hashTag1.hashTag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hashTag);
    }
}
