package com.backpackers.android.backend.model.feed;

import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

@Entity
@Cache
public class TimelineFeed {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<? extends AbstractPost> postKey;

    @Index
    private Date createdAt;

    private TimelineFeed() {
    }

    private TimelineFeed(Key<Account> parentUserKey,
                         Key<? extends AbstractPost> postKey,
                         Date createdAt) {
        this.parentUserKey = parentUserKey;
        this.postKey = postKey;
        this.createdAt = createdAt;
    }

    public static TimelineFeed newInstance(
            final Key<Account> parentUserKey,
            final Key<? extends AbstractPost> postKey,
            final Date createdAt) {
        return new TimelineFeed(parentUserKey, postKey, createdAt);
    }

    public Key<AbstractPost> getPostKey() {
        return (Key<AbstractPost>) postKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
