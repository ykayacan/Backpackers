package com.yoloo.android.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.model.feed.post.AdsPost;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.user.Account;

import java.util.Collection;
import java.util.Map;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeHelper {

    public static long getTotalPostLikesByUserKey(final Key<Account> userKey) {
        return ofy().load().type(Like.class).ancestor(userKey).keys().list().size();
    }

    public static LoadResult<Key<Like>> loadAsyncLikes(Key<Account> userKey,
                                                       Key<? extends Likeable> likeableKey) {
        // Async load
        return ofy().load().type(Like.class).ancestor(userKey)
                .filter("likeableEntityKey =", likeableKey)
                .keys().first();
    }

    public static void aggregateLikes(Collection<Post> posts,
                                      Map<Key<Post>, LoadResult<Key<Like>>> map) {
        for (Post post : posts) {
            Key<? extends Post> postKey = post.getKey();
            @SuppressWarnings("SuspiciousMethodCalls") // The key is always covariant.
                    LoadResult<Key<Like>> result = map.get(postKey);

            if (post instanceof TimelinePost) {
                if (result.now() != null) {
                    ((TimelinePost) post).setLiked(true);
                }

                ((TimelinePost) post).setLikeCount(getLikeCount(postKey));
            } else if (post instanceof AdsPost) {
                if (result.now() != null) {

                }
            }
        }
    }

    public static void aggregateLikes(TimelinePost post, LoadResult<Key<Like>> result) {
        if (result.now() != null) {
            post.setLiked(true);
        }
    }

    private static long getLikeCount(Key<? extends Post> postKey) {
        return ofy().load().type(Like.class)
                .filter("likeableEntityKey =", postKey)
                .keys().list().size();
    }
}
