package com.yoloo.android.backend.model.feed;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;

@Entity
@Cache
public class TimelineFeed<T extends Post> {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<T> postKey;

    @Index
    private Date createdAt;

    private TimelineFeed() {
    }

    private TimelineFeed(Key<Account> parentUserKey, Key<T> postKey, Date createdAt) {
        this.parentUserKey = parentUserKey;
        this.postKey = postKey;
        this.createdAt = createdAt;
    }

    public static <T extends Post> TimelineFeed<T> newInstance(
            final Key<Account> parentUserKey,
            final Key<T> postKey,
            final Date createdAt) {
        return new TimelineFeed<>(parentUserKey, postKey, createdAt);
    }

    public Key<T> getPostKey() {
        return postKey;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
