package com.yoloo.android.backend.counter.user;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.user.UserIndexShard;

public final class UserFollowerCounter implements Count {

    private final UserIndexShard shard;

    public UserFollowerCounter(UserIndexShard shard) {
        this.shard = shard;
    }

    @Override
    public void increase() {
        shard.setFollowerCount(shard.getFollowerCount() + 1);
    }

    @Override
    public void decrease() {
        if (shard.getFollowerCount() >= 0) {
            shard.setFollowerCount(shard.getFollowerCount() - 1);
        }
    }
}
