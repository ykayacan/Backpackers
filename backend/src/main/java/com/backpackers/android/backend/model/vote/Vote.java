package com.backpackers.android.backend.model.vote;

import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Cache
public class Vote {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<ForumPost> postKey;

    @Index
    private Status status = Status.DEFAULT;

    private Vote(final Key<Account> votedByKey,
                 final Key<ForumPost> postKey) {
        this.parentUserKey = votedByKey;
        this.postKey = postKey;
    }

    private Vote() {
    }

    public static Vote with(final Key<Account> votedByKey,
                            final Key<ForumPost> postKey) {
        return new Vote(votedByKey, postKey);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        DEFAULT, UP, DOWN
    }
}
