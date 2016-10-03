package com.backpackers.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.util.NotificationHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.model.comment.Commentable;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.like.Like;
import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.LikeHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.backpackers.android.backend.model.notification.Notification.Action.COMMENT;
import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class CommentController {

    private static final Logger logger =
            Logger.getLogger(CommentController.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    public static CommentController newInstance() {
        return new CommentController();
    }

    public Comment add(final String websafeCommentableId,
                       String text,
                       final User user) {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<? extends Commentable> commentableKey = Key.create(websafeCommentableId);

        Account account = null;
        ForumPost post = null;

        Map<Key<Object>, Object> map = ofy().load().keys(userKey, commentableKey);

        for (Object o : map.values()) {
            if (o instanceof Account) {
                account = (Account) o;
            } else if (o instanceof ForumPost) {
                post = (ForumPost) o;
            }
        }

        List<Object> saveList = new ArrayList<>(2);

        Comment comment = Comment.builder(commentableKey, userKey)
                .setComment(text)
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrl())
                .build();

        saveList.add(comment);

        // User is self so ignore notification.
        if (!user.getUserId().equals(post.getWebsafeOwnerId())) {
            if (text.length() > 100) {
                text = text.substring(0, 100).concat("...");
            }

            Notification notification = getNotification(account, Key.<Account>create(post.getWebsafeOwnerId()),
                    text, post.getWebsafeId());

            saveList.add(notification);

            final RegistrationRecord record = ofy().load().type(RegistrationRecord.class)
                    .ancestor(Key.<Account>create(post.getWebsafeOwnerId())).first().now();

            NotificationHelper.sendCommentNotification(record.getRegId(), text, websafeCommentableId);
        }

        ofy().save().entities(saveList).now();

        return comment;
    }

    public void remove(final String websafeCommentableId,
                       final String websafeCommentId,
                       final User user) {
        //Key<Account> userKey = Key.create(user.getUserId());
        //Key<? extends Commentable> commentableKey = Key.create(websafeCommentableId);
        Key<Comment> commentKey = Key.create(websafeCommentId);

        List<Key<Like>> likeKeys = ofy().load().type(Like.class)
                .filter("likeableEntityKey =", commentKey).keys().list();

        List<Object> deleteList = ImmutableList.builder()
                .add(commentKey)
                .addAll(likeKeys)
                .build();

        ofy().delete().entities(deleteList);
    }

    public CollectionResponse<Comment> list(final String websafeCommentableId,
                                            final String cursor,
                                            Integer limit,
                                            final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        final Key<? extends Commentable> commentableKey = Key.create(websafeCommentableId);
        final Key<Account> userKey = Key.create(user.getUserId());

        Query<Comment> query = ofy().load().type(Comment.class);

        query = query.filter("commentableKey", commentableKey);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.order("-createdAt");

        query = query.limit(limit);

        QueryResultIterator<Comment> queryIterator = query.iterator();

        // Store async batch in a hashmap. LinkedHashMap preserve insertion order.
        final Map<Key<Comment>, LoadResult<Key<Like>>> likeKeysMap =
                new LinkedHashMap<>(limit);

        final List<Comment> commentList = new ArrayList<>(limit);

        while (queryIterator.hasNext()) {
            Comment comment = queryIterator.next();
            Key<Comment> commentKey = comment.getKey();

            commentList.add(comment);
            likeKeysMap.put(commentKey, LikeHelper.loadAsyncCommentLikes(userKey, commentKey));
        }

        LikeHelper.aggregateCommentLikes(commentList, likeKeysMap);

        return CollectionResponse.<Comment>builder()
                .setItems(commentList)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    private Notification getNotification(Account sender, Key<Account> receiverKey,
                                         String comment, String postId) {
        return Notification.builder(sender.getKey(), receiverKey)
                .setAction(COMMENT)
                .setUsername(sender.getUsername())
                .setProfileImageUrl(sender.getProfileImageUrlLink())
                .setLocale(sender.getLocale())
                .setContent(comment)
                .setPostId(postId)
                .build();
    }
}
