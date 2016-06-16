package com.yoloo.android.backend.util;

import com.google.api.server.spi.response.NotFoundException;

import com.yoloo.android.backend.exception.AlreadyFoundException;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Follow;
import com.googlecode.objectify.Key;
import com.yoloo.android.backend.service.OfyHelper;

public final class FollowHelper {

    public static void follow(final Key<Account> followeeKey,
                              final Key<Account> followerKey) throws AlreadyFoundException {
        checkIsAlreadyFollowed(followeeKey, followerKey);

        final Follow follow = new Follow(followeeKey, followerKey);
        OfyHelper.ofy().save().entity(follow);

        // Increase followee count of follower.
        Account follower = follow.getFollower();
        follower.increaseFolloweeCount();

        OfyHelper.ofy().save().entity(follower);

        // Increase follower count of followee.
        Account followee = follow.getFollowee();
        followee.increaseFollowerCount();

        OfyHelper.ofy().save().entity(followee);
    }

    public static void unfollow(final Key<Account> followeeKey,
                                final Key<Account> followerKey) throws NotFoundException {
        final Follow follow = checkItemExists(followeeKey, followerKey);

        // Decrease followee count of follower.
        Account follower = follow.getFollower();
        follower.decreaseFolloweeCount();

        // Decrease follower count of followee.
        Account followee = follow.getFollowee();
        followee.decreaseFollowerCount();

        OfyHelper.ofy().delete().type(Follow.class).id(follow.getId());
        OfyHelper.ofy().save().entities(follower, followee);
    }

    private static Follow checkItemExists(final Key<Account> followeeKey,
                                          final Key<Account> followerKey) throws
            NotFoundException {

        try {
            return OfyHelper.ofy().load().type(Follow.class)
                    .filter("followeeRef", followeeKey)
                    .filter("followerRef", followerKey)
                    .first().safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find followee ID: " + followeeKey.getId());
        }
    }

    private static void checkIsAlreadyFollowed(final Key<Account> followeeKey,
                                               final Key<Account> followerKey) throws
            AlreadyFoundException {
        Key<Follow> followKey = OfyHelper.ofy().load().type(Follow.class)
                .filter("followeeRef", followeeKey)
                .filter("followerRef", followerKey)
                .keys().first().now();

        if (followKey != null) {
            throw new AlreadyFoundException("You already liked.");
        }
    }
}
