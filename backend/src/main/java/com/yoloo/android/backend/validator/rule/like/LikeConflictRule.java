package com.yoloo.android.backend.validator.rule.like;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeConflictRule implements Rule<ConflictException> {

    private final String websafeKey;
    private final User user;

    public LikeConflictRule(String websafeKey, User user) {
        this.websafeKey = websafeKey;
        this.user = user;
    }

    @Override
    public void validate() throws ConflictException {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<?> likeableEntityKey = Key.create(websafeKey);

        Key<?> key = ofy().load().type(Like.class)
                .ancestor(userKey)
                .filter("likeableEntity =", likeableEntityKey)
                .keys().first().now();

        if (key != null) {
            throw new ConflictException("Already liked.");
        }
    }
}
