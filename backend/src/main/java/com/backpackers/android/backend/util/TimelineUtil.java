package com.backpackers.android.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.follow.Follow;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.servlet.CreateTimelineServlet;

public class TimelineUtil {

    public static void updateTimeline(Key<Account> userKey,
                                      LoadResult<Key<Follow>> followResult,
                                      AbstractPost abstractPost) {
        // The user is followed by someone.
        if (followResult.now() != null) {
            // Write post to user's followers timeline.
            CreateTimelineServlet.create(
                    userKey.toWebSafeString(),
                    abstractPost.getWebsafeId(),
                    String.valueOf(abstractPost.getCreatedAt().getTime()));
        }
    }
}
