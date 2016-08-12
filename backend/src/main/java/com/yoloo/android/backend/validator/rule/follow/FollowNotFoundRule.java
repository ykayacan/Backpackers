package com.yoloo.android.backend.validator.rule.follow;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class FollowNotFoundRule implements Rule<NotFoundException> {

    private final String websafeFolloweeKey;
    private final User user;

    public FollowNotFoundRule(String websafeFolloweeKey, User user) {
        this.websafeFolloweeKey = websafeFolloweeKey;
        this.user = user;
    }

    @Override
    public void validate() throws NotFoundException {
        try {
            Key<Account> followerKey = Key.create(user.getUserId());
            Key<Account> followeeKey = Key.create(websafeFolloweeKey);

            ofy().load().type(Follow.class)
                    .ancestor(followerKey)
                    .filter("followeeKey =", followeeKey)
                    .keys().first().safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Follow.");
        }
    }
}
