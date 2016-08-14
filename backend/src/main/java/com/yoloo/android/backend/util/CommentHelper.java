package com.yoloo.android.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.user.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class CommentHelper {

    public static LoadResult<Key<Comment>> loadAsyncComments(Key<Account> userKey,
                                                             Key<? extends Post> commentableKey) {
        // Async load
        return ofy().load().type(Comment.class).ancestor(userKey)
                .filter("commentableKey =", commentableKey)
                .keys().first();
    }

    public static void aggregateComments(List<ForumPost> posts,
                                         Map<Key<? extends Post>,
                                                 LoadResult<Key<Comment>>> map) {
        for (Post post : posts) {
            // The key is always covariant.
            @SuppressWarnings("SuspiciousMethodCalls")
            LoadResult<Key<Comment>> result = map.get(post.getKey());

            aggregateComments(post, result);
        }
    }

    public static void aggregateComments(Collection<Post> posts,
                                         Map<Key<? extends Post>,
                                                 LoadResult<Key<Comment>>> map) {
        for (Post post : posts) {
            // The key is always covariant.
            @SuppressWarnings("SuspiciousMethodCalls")
            LoadResult<Key<Comment>> result = map.get(post.getKey());

            aggregateComments(post, result);
        }
    }

    public static void aggregateComments(Post post, LoadResult<Key<Comment>> result) {
        if (post instanceof TimelinePost) {
            if (result.now() != null) {
                ((TimelinePost) post).setCommented(true);
            }
        } else if (post instanceof ForumPost) {
            if (result.now() != null) {
                ((ForumPost) post).setCommented(true);
            }
            ((ForumPost) post).setComments(getCommentCount(post.getKey()));
        }
    }

    private static long getCommentCount(Key<? extends Post> commentableKey) {
        return ofy().load().type(Comment.class)
                .filter("commentableKey =", commentableKey)
                .keys().list().size();
    }
}