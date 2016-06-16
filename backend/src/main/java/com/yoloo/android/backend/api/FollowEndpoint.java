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

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Follow;
import com.yoloo.android.backend.util.EndpointUtil;
import com.yoloo.android.backend.util.FollowHelper;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.IdValidationRule;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.yoloo.android.backend.service.OfyHelper.ofy;
import static com.yoloo.android.backend.util.EndpointUtil.isValidToken;

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
        audiences = {Constants.AUDIENCE_ID}
)
public class FollowEndpoint {

    private static final Logger logger = Logger.getLogger(FollowEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Inserts a new {@code Follow}.
     */
    @ApiMethod(
            name = "follow",
            path = "accounts/{id}/follows",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void follow(@Named("id") final long followeeId,
                       @Named("access_token") final String accessToken) throws
            ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(followeeId));
        validator.validate();

        Key<Account> followerKey = isValidToken(accessToken);

        EndpointUtil.checkItemExists(Account.class, followeeId);

        FollowHelper.follow(Key.create(Account.class, followeeId), followerKey);
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
            path = "accounts/{id}/follows",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final long followeeId,
                       @Named("access_token") final String accessToken) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(followeeId));
        validator.validate();

        Key<Account> accountKey = isValidToken(accessToken);

        EndpointUtil.checkItemExists(Account.class, followeeId);

        FollowHelper.unfollow(Key.create(Account.class, followeeId), accountKey);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listFollows",
            path = "accounts/{id}/follows",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Follow> list(@Named("id") final long followerId,
                                           @Named("type") final String type,
                                           @Nullable @Named("cursor") String cursor,
                                           @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Follow> query = ofy().load().type(Follow.class);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        if (type.contentEquals("follower")) {
            query = query.filter("followerRef =", Key.create(Follow.class, followerId));
        } else if (type.contentEquals("followee")) {
            query = query.filter("followeeRef =", Key.create(Follow.class, followerId));
        }
        query = query.limit(limit);

        QueryResultIterator<Follow> queryIterator = query.iterator();
        List<Follow> followList = new ArrayList<Follow>(limit);
        while (queryIterator.hasNext()) {
            followList.add(queryIterator.next());
        }
        return CollectionResponse.<Follow>builder().setItems(followList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }
}