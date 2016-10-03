package com.backpackers.android.backend.model.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

@Entity
@Cache(expirationSeconds = 60)
public class UserIndexShardCounter {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    private int shardCount;

    private UserIndexShardCounter() {
    }

    private UserIndexShardCounter(Key<Account> parentUserKey) {
        this.parentUserKey = parentUserKey;
    }

    public static UserIndexShardCounter newInstance(Key<Account> parentUserKey) {
        return new UserIndexShardCounter(parentUserKey);
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }
}
