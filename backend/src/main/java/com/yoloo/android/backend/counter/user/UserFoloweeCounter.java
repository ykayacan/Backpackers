package com.yoloo.android.backend.counter.user;

import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.model.user.UserIndexShard;

public final class UserFoloweeCounter implements Count {

    private final UserIndexShard counter;

    public UserFoloweeCounter(UserIndexShard shard) {
        this.counter = shard;
    }

    @Override
    public void increase() {
        counter.setFolloweeCount(counter.getFolloweeCount() + 1);
    }

    @Override
    public void decrease() {
        if (counter.getFolloweeCount() >= 0) {
            counter.setFolloweeCount(counter.getFolloweeCount() - 1);
        }
    }
}
