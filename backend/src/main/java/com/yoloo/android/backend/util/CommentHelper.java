package com.yoloo.android.backend.util;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.googlecode.objectify.Key;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public final class CommentHelper {

    public static Comment createComment(final Key<Account> accountKey,
                                        final Key<Feed> feedKey,
                                        final String text) {
        Account account = ofy().load().type(Account.class)
                .id(accountKey.getId()).now();

        Comment comment = new Comment.Builder()
                .setAccount(accountKey)
                .setFeed(feedKey)
                .setAccountPhotoUrl(account.getPictureUrl().getValue())
                .setUsername(account.getUsername())
                .setComment(text)
                .build();

        ofy().save().entity(comment).now();

        Feed feed = comment.getFeed();
        feed.increaseCommentCounter();
        ofy().save().entity(feed);

        return comment;
    }

    public static void removeComment(final long id) {
        Comment comment = ofy().load().type(Comment.class).id(id).now();

        Feed feed = comment.getFeed();
        feed.decreaseCommentCounter();
        ofy().save().entity(feed);

        LikeHelper.deleteChildLike(Comment.class, Key.create(Comment.class, id));
        ofy().delete().type(Comment.class).id(id);
    }
}
