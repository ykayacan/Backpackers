package com.yoloo.android.backend.util;

import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

import com.yoloo.android.backend.exception.InvalidIdException;
import com.yoloo.android.backend.exception.InvalidTokenException;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Deletable;
import com.yoloo.android.backend.service.OfyHelper;
import com.googlecode.objectify.Key;

public final class EndpointUtil {

    /**
     * Default constructor, never called.
     */
    private EndpointUtil() {
    }

    /**
     * Throws an exception if the user object doesn't represent an authenticated
     * call.
     *
     * @param user User object to be checked if it represents an authenticated
     *             caller.
     * @throws com.google.api.server.spi.response.UnauthorizedException when the
     *                                                                  user object does not
     *                                                                  represent an admin.
     */
    public static void throwIfNotAuthenticated(final User user) throws
            UnauthorizedException {
        if (user == null || user.getEmail() == null) {
            throw new UnauthorizedException(
                    "Only authenticated users may invoke this operation");
        }
    }

    public static Key<Account> isValidToken(String token) throws
            InvalidTokenException {
        Key<Account> accountKey = OfyHelper.ofy().load().type(Account.class)
                .filter("accessToken =", token).keys().first().now();
        if (accountKey == null) {
            throw new InvalidTokenException();
        }
        return accountKey;
    }

    public static <T> T checkItemExists(final Class<? extends T> type, final long id) throws
            NotFoundException {
        try {
            return OfyHelper.ofy().load().type(type).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find " + type.getSimpleName() + " with ID: " + id);
        }
    }

    public static void checkIsAuthorizedToRemove(final Class<? extends Deletable> type,
                                                 final long id,
                                                 final Key<Account> accountKey) throws
            InvalidIdException {
        Deletable item = OfyHelper.ofy().load().type(type).id(id).now();

        if (!item.getAccountKey().equivalent(accountKey)) {
            throw new InvalidIdException("You don't have permissions to delete " + type.getSimpleName());
        }
    }
}
