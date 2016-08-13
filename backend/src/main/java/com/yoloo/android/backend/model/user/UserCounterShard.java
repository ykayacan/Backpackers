package com.yoloo.android.backend.model.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache(expirationSeconds = 60)
public class UserCounterShard {

    @Id
    private long shardId;

    @Parent
    private Key<Account> user;

    /**
     * The parentUserKey follower count.
     */
    @Index(IfNotDefault.class)
    private long followeeCount = 0;

    /**
     * The parentUserKey following count.
     */
    @Index(IfNotDefault.class)
    private long followerCount = 0;

    /**
     * The parentUserKey question count.
     */
    @Index(IfNotDefault.class)
    private long questionCount = 0;

    private UserCounterShard() {
    }

    private UserCounterShard(Builder builder) {
        this.shardId = builder.shardId;
        this.user = builder.userKey;
    }

    public static UserCounterShard.Builder builder(long shardId, Key<Account> parentUserKey) {
        return new UserCounterShard.Builder(shardId, parentUserKey);
    }

    public long getFolloweeCount() {
        return followeeCount;
    }

    public void setFolloweeCount(long followeeCount) {
        this.followeeCount = followeeCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(long questionCount) {
        this.questionCount = questionCount;
    }

    public static final class Builder {
        private long shardId;
        private Key<Account> userKey;

        public Builder(long shardId, Key<Account> parentUserKey) {
            this.shardId = shardId;
            this.userKey = parentUserKey;
        }

        public UserCounterShard build() {
            return new UserCounterShard(this);
        }
    }
}
