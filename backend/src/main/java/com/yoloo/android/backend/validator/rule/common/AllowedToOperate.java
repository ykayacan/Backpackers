package com.yoloo.android.backend.validator.rule.common;

import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Rule;

public class AllowedToOperate implements Rule<ForbiddenException> {

    private final Class<?> type;
    private final String websafeKey;
    private final User user;
    private final String operation;

    public AllowedToOperate(Class<?> type, String websafeKey, User user, String operation) {
        this.type = type;
        this.websafeKey = websafeKey;
        this.user = user;
        this.operation = operation;
    }

    @Override
    public void validate() throws ForbiddenException {
        Key<?> key = Key.create(websafeKey);
        Key<Account> userKey = Key.create(user.getUserId());

        if (type == Comment.class) {
            if (key.getParent().getParent().compareTo(userKey) != 0) {
                throw new ForbiddenException("You don't have permissions to " +
                        operation + "" + type.getSimpleName().toLowerCase());
            }
        } else if (key.getParent().compareTo(userKey) != 0) {
            throw new ForbiddenException("You don't have permissions to " +
                    operation + "" + type.getSimpleName().toLowerCase());
        }
    }
}
