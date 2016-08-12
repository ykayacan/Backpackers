package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.controller.UserController;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.follow.FollowConflictRule;
import com.yoloo.android.backend.validator.rule.follow.FollowNotFoundRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

@Api(
        name = "yolooApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(
        resource = "follows",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class}
)
public class FollowEndpoint {

    private static final Logger logger =
            Logger.getLogger(FollowEndpoint.class.getSimpleName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Inserts a new {@code Follow}.
     */
    @ApiMethod(
            name = "follow",
            path = "users/{user_id}/follows",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void follow(@Named("user_id") final String followeeId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(followeeId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Account.class, followeeId))
                .addRule(new FollowConflictRule(followeeId, user))
                .validate();

        UserController.newInstance().follow(followeeId, user);
    }

    /**
     * Deletes the specified {@code Follow}.
     *
     * @param followeeId the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Follow}
     */
    @ApiMethod(
            name = "unfollow",
            path = "users/{user_id}/follows",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unfollow(@Named("user_id") final String followeeId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(followeeId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Account.class, followeeId))
                .addRule(new FollowNotFoundRule(followeeId, user))
                .validate();

        UserController.newInstance().unfollow(followeeId, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "follows.list",
            path = "users/{user_id}/follows",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> list(@Named("user_id") String userId,
                                            @Named("type") final String type,
                                            @Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit,
                                            final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        Key<Account> parentUserKey = Key.create(userId);

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Follow> query = ofy().load().type(Follow.class);

        switch (type) {
            case "followee":
                query = query.ancestor(parentUserKey);
                break;
            case "follower":
                query = query.filter("followeeKey =", parentUserKey);
                break;
            default:
                break;
        }

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        query = query.limit(limit);

        QueryResultIterator<Follow> queryIterator = query.iterator();
        List<Key<Account>> followKeys = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            if (type.contentEquals("follower")) {
                followKeys.add(queryIterator.next().getParentUserKey());
            } else if (type.contentEquals("followee")) {
                followKeys.add(queryIterator.next().getFolloweeKey());
            }
        }

        Collection<Account> users = ofy().load().keys(followKeys).values();

        return CollectionResponse.<Account>builder()
                .setItems(users)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}