package com.yoloo.android.backend.validator.rule.like;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

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

        final Key<Like> key = ofy().load().type(Like.class)
                .ancestor(userKey)
                .filter("likeableEntityKey =", likeableKey)
                .keys().first().now();

        if (key != null) {
            throw new ConflictException("Already liked.");
        }
    }
}
