package com.yoloo.android.backend.modal;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import lombok.Getter;

@Entity
@Cache
public class Follow {

    /**
     * The id for the datastore key.
     *
     * We use automatic id assignment for entities of Follower class.
     */
    @Id
    @Getter
    private Long id;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> followeeRef;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> followerRef;

    private Follow() {
    }

    public Follow(final Key<Account> followeeKey, final Key<Account> followerKey) {
        this.followeeRef = Ref.create(followeeKey);
        this.followerRef = Ref.create(followerKey);
    }

    public Account getFollowee() {
        return this.followeeRef.get();
    }

    public Account getFollower() {
        return this.followerRef.get();
    }
}
