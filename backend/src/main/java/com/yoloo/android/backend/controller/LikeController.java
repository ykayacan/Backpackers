package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.user.Account;

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

    private LikeController(User user, String websafeLikeableId) {
        this.user = user;
        this.websafeEntityKey = websafeLikeableId;
    }

    public static LikeController newInstance(User user, String websafeLikeableId) {
        return new LikeController(user, websafeLikeableId);
    }

    public void like() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Likeable> likeableKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Like like = Like.with(userKey, likeableKey);

                ofy().save().entity(like);
            }
        });
    }

    public void dislike() {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Likeable> likeableKey = Key.create(websafeEntityKey);

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Key<Like> likeKey = ofy().load()
                        .type(Like.class).ancestor(userKey)
                        .filter("likeableEntityKey =", likeableKey).keys().first().now();

                ofy().delete().key(likeKey);
            }
        });
    }
}
