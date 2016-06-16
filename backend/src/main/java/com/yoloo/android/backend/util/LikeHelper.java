package com.yoloo.android.backend.util;

import com.google.api.server.spi.response.NotFoundException;

import com.yoloo.android.backend.exception.AlreadyFoundException;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.modal.like.CommentLike;
import com.yoloo.android.backend.modal.like.FeedLike;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.QueryKeys;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public final class LikeHelper {

    private static <T> void checkLikeExists(final Key<T> itemKey,
                                            final Key<Account> accountKey,
                                            final Class<?> clazz) throws NotFoundException {
        try {
            if (clazz == FeedLike.class) {
                ofy().load().type(clazz).filter("feedRef =", itemKey)
                        .filter("accountRef =", accountKey).keys().first().safe();
            } else if (clazz == CommentLike.class) {
                ofy().load().type(clazz).filter("commentRef =", itemKey)
                        .filter("accountRef =", accountKey).keys().first().safe();
            }
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Like");
        }
    }

    private static void checkIsFeedAlreadyLiked(final Key<Feed> feedKey,
                                                final Key<Account> accountKey) throws AlreadyFoundException {
        Key<FeedLike> likeKey = ofy().load().type(FeedLike.class)
                .filter("feedRef =", feedKey)
                .filter("accountRef =", accountKey)
                .keys().first().now();
        if (likeKey != null) {
            throw new AlreadyFoundException("You already liked.");
        }
    }

    public static boolean checkIsFeedLiked(final Key<Feed> feedKey,
                                           final Key<Account> accountKey) {
        return ofy().load().type(FeedLike.class)
                .filter("feedRef =", feedKey)
                .filter("accountRef =", accountKey)
                .keys().first().now() != null;
    }

    private static void checkIsCommentAlreadyLiked(final Key<Comment> commentKey,
                                                   final Key<Account> accountKey) throws AlreadyFoundException {
        Key<CommentLike> commentLikeKey = ofy().load().type(CommentLike.class)
                .filter("commentRef =", commentKey)
                .filter("accountRef =", accountKey)
                .keys().first().now();
        if (commentLikeKey != null) {
            throw new AlreadyFoundException("You already liked.");
        }
    }

    public static void likeFeed(final Key<Feed> feedKey,
                                final Key<Account> accountKey) throws AlreadyFoundException {
        checkIsFeedAlreadyLiked(feedKey, accountKey);

        FeedLike like = new FeedLike(accountKey, feedKey);

        Feed feed = like.getFeed();
        feed.increaseLikeCounter();

        ofy().save().entity(like);
        ofy().save().entity(feed);
    }

    public static void unlikeFeed(final Key<Feed> feedKey,
                                  final Key<Account> accountKey) throws NotFoundException {
        checkLikeExists(feedKey, accountKey, FeedLike.class);

        FeedLike like = ofy().load().type(FeedLike.class)
                .filter("feedRef =", feedKey)
                .filter("accountRef =", accountKey).first().now();

        Feed feed = like.getFeed();
        feed.decreaseLikeCounter();

        ofy().save().entity(feed);
        ofy().delete().type(FeedLike.class).id(like.getId());
    }

    public static void likeComment(final Key<Comment> commentKey,
                                   final Key<Account> accountKey) throws AlreadyFoundException {
        checkIsCommentAlreadyLiked(commentKey, accountKey);

        CommentLike like = new CommentLike(accountKey, commentKey);

        Comment comment = like.getComment();
        comment.increaseLikeCounter();

        ofy().save().entity(like);
        ofy().save().entity(comment);
    }

    public static void unlikeComment(final Key<Comment> commentKey,
                                     final Key<Account> accountKey) throws NotFoundException {
        checkLikeExists(commentKey, accountKey, CommentLike.class);

        CommentLike like = ofy().load().type(CommentLike.class)
                .filter("commentRef =", commentKey)
                .filter("accountRef =", accountKey).first().now();

        Comment comment = like.getComment();
        comment.decreaseLikeCounter();

        ofy().save().entity(comment);
        ofy().delete().type(CommentLike.class).id(like.getId());
    }

    public static <T> void deleteChildLikes(Class<? extends T> clazz,
                                            final Key<? extends T>... itemKeys) {
        for (Key<? extends T> key : itemKeys) {
            deleteChildLike(clazz, key);
        }
    }

    public static <T> void deleteChildLike(Class<? extends T> clazz,
                                           final Key<? extends T> itemKey) {
        QueryKeys<?> keys = null;
        if (clazz == Feed.class) {
            keys = ofy().load().type(FeedLike.class)
                    .filter("feedRef =", itemKey).keys();
        } else if (clazz == Comment.class) {
            keys = ofy().load().type(CommentLike.class)
                    .filter("commentRef =", itemKey).keys();
        }
        ofy().delete().keys(keys);
    }
}
