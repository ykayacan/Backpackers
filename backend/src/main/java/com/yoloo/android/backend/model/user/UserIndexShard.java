package com.yoloo.android.backend.model.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache(expirationSeconds = 60)
public class UserIndexShard {

    @Id
    private long shardId;

    @Parent
    @Load
    private Ref<Account> user;

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

    private UserIndexShard() {
    }

    private UserIndexShard(Builder builder) {
        this.shardId = builder.shardId;
        this.user = builder.user;
    }

    public static UserIndexShard.Builder builder(long shardId, Key<Account> parentUserKey) {
        return new UserIndexShard.Builder(shardId, parentUserKey);
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
        private Ref<Account> user;

        public Builder(long shardId, Key<Account> parentUserKey) {
            this.shardId = shardId;
            this.user = Ref.create(parentUserKey);
        }

        public UserIndexShard build() {
            return new UserIndexShard(this);
        }
    }
}
