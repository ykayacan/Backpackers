package com.yoloo.android.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.servlet.CreateTimelineServlet;

public class TimelineUtil {

    public static void updateTimeline(Key<Account> userKey,
                                      LoadResult<Key<Follow>> followResult,
                                      Post post) {
        // The user is followed by someone.
        if (followResult.now() != null) {
            // Write post to user's followers timeline.
            CreateTimelineServlet.create(
                    userKey.toWebSafeString(),
                    post.getWebsafeId(),
                    String.valueOf(post.getCreatedAt().getTime()));
        }
    }
}
