package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.comment.Commentable;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.user.Account;

import java.util.List;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class CommentController {

    private static final Logger logger =
            Logger.getLogger(CommentController.class.getName());

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
        Key<Account> userKey = Key.create(user.getUserId());
        Key<? extends Commentable> commentableKey = Key.create(websafeCommentableId);
        Key<Comment> commentKey = Key.create(websafeCommentId);

        List<Key<Like>> likeKeys = ofy().load().type(Like.class)
                .filter("likeableEntityKey =", commentKey).keys().list();

        List<Object> deleteList = ImmutableList.builder()
                .add(commentKey)
                .addAll(likeKeys)
                .build();

        ofy().delete().entities(deleteList);
    }
}
