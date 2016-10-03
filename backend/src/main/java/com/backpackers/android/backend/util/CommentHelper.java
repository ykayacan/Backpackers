package com.backpackers.android.backend.util;

import com.backpackers.android.backend.model.comment.Comment;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.feed.post.NormalPost;
import com.backpackers.android.backend.model.user.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class CommentHelper {

    public static LoadResult<Key<Comment>> loadAsyncComments(Key<Account> userKey,
                                                             Key<? extends AbstractPost> commentableKey) {
        // Async load
        return ofy().load().type(Comment.class).ancestor(userKey)
                .filter("commentableKey =", commentableKey)
                .keys().first();
    }

    public static void aggregateComments(List<ForumPost> posts,
                                         Map<Key<? extends AbstractPost>,
                                                 LoadResult<Key<Comment>>> map) {
        for (AbstractPost abstractPost : posts) {
            // The key is always covariant.
            @SuppressWarnings("SuspiciousMethodCalls")
            LoadResult<Key<Comment>> result = map.get(abstractPost.getKey());

            aggregateComments(abstractPost, result);
        }
    }

    public static void aggregateComments(Collection<AbstractPost> abstractPosts,
                                         Map<Key<? extends AbstractPost>,
                                                 LoadResult<Key<Comment>>> map) {
        for (AbstractPost abstractPost : abstractPosts) {
            // The key is always covariant.
            @SuppressWarnings("SuspiciousMethodCalls")
            LoadResult<Key<Comment>> result = map.get(abstractPost.getKey());

            aggregateComments(abstractPost, result);
        }
    }

    public static void aggregateComments(AbstractPost abstractPost, LoadResult<Key<Comment>> result) {
        if (abstractPost instanceof NormalPost) {
            if (result.now() != null) {
                ((NormalPost) abstractPost).setCommented(true);
            }
        } else if (abstractPost instanceof ForumPost) {
            if (result.now() != null) {
                ((ForumPost) abstractPost).setCommented(true);
            }
            ((ForumPost) abstractPost).setComments(getCommentCount(abstractPost.getKey()));
        }
    }

    private static long getCommentCount(Key<? extends AbstractPost> commentableKey) {
        return ofy().load().type(Comment.class)
                .filter("commentableKey =", commentableKey)
                .keys().list().size();
    }
}