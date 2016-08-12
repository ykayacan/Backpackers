package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.like.LikeEntity;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.ClassUtil;

import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeController {

    /**
     * A logger object.
     */
    private static final Logger logger =
            Logger.getLogger(LikeController.class.getName());

    private final User user;

    private final String websafeEntityKey;

    private LikeController(User user, String websafeEntityKey) {
        this.user = user;
        this.websafeEntityKey = websafeEntityKey;
    }

    public static LikeController newInstance(User user, String websafeEntityKey) {
        return new LikeController(user, websafeEntityKey);
    }

    public void like() {
        final Key<?> key = Key.create(websafeEntityKey);

        switch (key.getKind()) {
            case "TimelinePost":
            case "AdsPost":
                likePost();
                break;
            case "Comment":
                likeComment();
                break;
        }
    }

    public void dislike() {
        final Key<?> key = Key.create(websafeEntityKey);

        switch (key.getKind()) {
            case "TimelinePost":
            case "AdsPost":
                dislikePost();
                break;
            case "Comment":
                dislikeComment();
                break;
        }
    }

    private void likePost() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Post> postKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                LikeEntity<Post> likeEntity = LikeEntity.with(userKey, postKey);

                ofy().save().entity(likeEntity);
            }
        });
    }

    private void dislikePost() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Post> postKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Key<LikeEntity<Post>> likeKey = ofy().load()
                        .type(ClassUtil.<LikeEntity<Post>>castClass(LikeEntity.class))
                        .ancestor(userKey)
                        .filter("postKey =", postKey).keys().first().now();

                ofy().delete().key(likeKey);
            }
        });
    }

    private void likeComment() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Comment> commentKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                LikeEntity<Comment> likeEntity = LikeEntity.with(userKey, commentKey);

                ofy().save().entity(likeEntity);
            }
        });
    }

    private void dislikeComment() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Post> postKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Key<LikeEntity<Comment>> likeKey = ofy().load()
                        .type(ClassUtil.<LikeEntity<Comment>>castClass(LikeEntity.class))
                        .ancestor(userKey)
                        .filter("postKey =", postKey).keys().first().now();

                ofy().delete().key(likeKey);
            }
        });
    }

}
