package com.backpackers.android.backend.validator.rule.like;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.model.like.Like;
import com.backpackers.android.backend.model.like.Likeable;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.googlecode.objectify.Key;
import com.backpackers.android.backend.validator.Rule;

public class LikeConflictRule implements Rule<ConflictException> {

    private final String websafePostId;
    private final User user;

    public LikeConflictRule(String websafePostId, User user) {
        this.websafePostId = websafePostId;
        this.user = user;
    }

    @Override
    public void validate() throws ConflictException {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<? extends Likeable> likeableKey = Key.create(websafePostId);

        final Key<Like> key = OfyHelper.ofy().load().type(Like.class)
                .ancestor(userKey)
                .filter("likeableEntityKey =", likeableKey)
                .keys().first().now();

        if (key != null) {
            throw new ConflictException("Already liked.");
        }
    }
}
