package com.yoloo.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.comment.Commentable;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LikeHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class CommentController {

    private static final Logger logger =
            Logger.getLogger(CommentController.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    public static CommentController newInstance() {
        return new CommentController();
    }

    public Comment add(final String websafeCommentableId,
                       final String text,
                       final User user) {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<? extends Commentable> commentableKey = Key.create(websafeCommentableId);

        Account account = ofy().load().key(userKey).now();

        Comment comment = Comment.builder(commentableKey, userKey)
                .setComment(text)
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrl())
                .build();

        ofy().save().entity(comment).now();

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
}
