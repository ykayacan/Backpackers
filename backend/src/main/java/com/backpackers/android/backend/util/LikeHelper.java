package com.backpackers.android.backend.util;

import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.like.Like;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.like.Likeable;
import com.backpackers.android.backend.model.user.Account;

import java.util.Collection;
import java.util.Map;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class LikeHelper {

    public static long getTotalPostLikesByUserKey(final Key<Account> userKey) {
        return ofy().load().type(Like.class).ancestor(userKey).keys().list().size();
    }

    public static LoadResult<Key<Like>> loadAsyncPostLikes(Key<Account> userKey,
                                                           Key<? extends AbstractPost> postKey) {
        // Async load
        return ofy().load().type(Like.class).ancestor(userKey)
                .filter("likeableEntityKey =", postKey)
                .keys().first();
    }

    public static LoadResult<Key<Like>> loadAsyncCommentLikes(Key<Account> userKey,
                                                              Key<Comment> commentKey) {
        // Async load
        return ofy().load().type(Like.class).ancestor(userKey)
                .filter("likeableEntityKey =", commentKey)
                .keys().first();
    }

    public static void aggregatePostLikes(Collection<AbstractPost> posts,
                                          Map<Key<AbstractPost>, LoadResult<Key<Like>>> map) {
        for (AbstractPost post : posts) {
            if (!(post instanceof ForumPost)) {
                Key<? extends Likeable> entityKey = ((Likeable) post).getLikeableKey();
                // The key is always covariant.
                @SuppressWarnings("SuspiciousMethodCalls")
                final LoadResult<Key<Like>> result = map.get(entityKey);

                aggregateLike((Likeable) post, result);
            }
        }
    }

    public static void aggregateCommentLikes(Collection<Comment> comments,
                                             Map<Key<Comment>, LoadResult<Key<Like>>> map) {
        for (Comment comment : comments) {
            LoadResult<Key<Like>> result = map.get(comment.getKey());

            aggregateLike(comment, result);
        }
    }

    public static void aggregateLike(Likeable likeable, LoadResult<Key<Like>> result) {
        likeable.setEntityLiked(result.now() != null);
        likeable.setEntityLikes(getLikeCount(likeable.getLikeableKey()));
    }

    private static long getLikeCount(Key<? extends Likeable> likeableKey) {
        return ofy().load().type(Like.class)
                .filter("likeableEntityKey =", likeableKey)
                .keys().list().size();
    }
}
