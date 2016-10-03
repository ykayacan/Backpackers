package com.backpackers.android.backend.model.follow;

import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Cache
public class Follow {

    /**
     * The id for the datastore key.
     *
     * We use automatic id assignment for entities of Follower class.
     */
    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<Account> followeeKey;

    private Follow() {
    }

    private Follow(Key<Account> parentUserKey,
                   Key<Account> followeeKey) {
        this.parentUserKey = parentUserKey;
        this.followeeKey = followeeKey;
    }

    /**
     * New instance follow.
     *
     * @param parentUserKey the parent parentUserKey key
     * @param followeeKey   the followee key
     * @return the follow
     */
    public static Follow newInstance(Key<Account> parentUserKey,
                                     Key<Account> followeeKey) {
        return new Follow(parentUserKey, followeeKey);
    }

    /**
     * Gets parent parentUserKey key.
     *
     * @return the parent parentUserKey key
     */
    public Key<Account> getParentUserKey() {
        return parentUserKey;
    }

    /**
     * Gets followee key.
     *
     * @return the followee key
     */
    public Key<Account> getFolloweeKey() {
        return followeeKey;
    }
}
