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

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.request.FeedRequest;
import com.yoloo.android.backend.util.EndpointUtil;
import com.yoloo.android.backend.util.FeedHelper;
import com.yoloo.android.backend.util.LikeHelper;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.FeedRequestRule;
import com.yoloo.android.backend.validator.rule.IdValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.yoloo.android.backend.service.OfyHelper.ofy;
import static com.yoloo.android.backend.util.EndpointUtil.checkItemExists;
import static com.yoloo.android.backend.util.EndpointUtil.isValidToken;

/**
 * The type Feed endpoint.
 */
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
        resource = "feeds",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class FeedEndpoint {

    /**
     * Log output.
     */
    private static final Logger logger = Logger.getLogger(FeedEndpoint.class.getName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Returns the {@link Feed} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Feed} with the provided ID.
     */
    @ApiMethod(
            name = "getFeed",
            path = "feeds/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Feed get(@Named("id") final long id) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Feed feed = checkItemExists(Feed.class, id);
        Key<Feed> feedKey = feed.getKey();
        feed.setHashtags(FeedHelper.fetchHashtags(feedKey));
        feed.setLocations(FeedHelper.fetchHashtags(feedKey));

        return feed;
    }

    /**
     * Inserts a new {@code Feed}.
     */
    @ApiMethod(
            name = "addFeed",
            path = "feeds",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Feed add(final FeedRequest request,
                    @Named("access_token") final String accessToken) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new FeedRequestRule(request));
        validator.validate();

        // Validate access token.
        Key<Account> accountKey = isValidToken(accessToken);

        return FeedHelper.createFeed(accountKey, request);
    }

    /**
     * Updates an existing {@code Feed}.
     *
     * @param id      the ID of the entity to be updated
     * @param request the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "updateFeed",
            path = "feeds/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Feed update(@Named("id") final long id,
                       @Named("access_token") final String accessToken,
                       final FeedRequest request) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.addRule(new FeedRequestRule(request));
        validator.validate();

        // Validate access token.
        Key<Account> accountKey = isValidToken(accessToken);

        checkItemExists(Feed.class, id);

        return FeedHelper.createFeed(accountKey, request);
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "removeFeed",
            path = "feeds/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final long id,
                       @Named("access_token") final String accessToken) throws ServiceException {

        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);
        checkItemExists(Feed.class, id);
        EndpointUtil.checkIsAuthorizedToRemove(Feed.class, id, accountKey);

        FeedHelper.removeFeed(id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listFeed",
            path = "feeds",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Feed> list(@Nullable @Named("user_id") final Long userId,
                                         @Nullable @Named("sort") final String[] sort,
                                         @Nullable @Named("cursor") final String cursor,
                                         @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Feed> query = ofy().load().type(Feed.class);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        if (sort != null && sort.length > 0) {
            for (String option : sort) {
                if (option.contentEquals("created_at")) {
                    query = query.order("createdAt");
                }
                if (option.contentEquals("-created_at")) {
                    query = query.order("-createdAt");
                }
                if (option.contentEquals("like")) {
                    query = query.order("likesCount");
                }
                if (option.contentEquals("-like")) {
                    query = query.order("-likesCount");
                }
            }
        }

        query = query.limit(limit);

        for (Feed feed : query.list()) {
            Key<Feed> feedKey = feed.getKey();
            feed.setHashtags(FeedHelper.fetchHashtags(feedKey));
            feed.setLocations(FeedHelper.fetchHashtags(feedKey));
        }

        if (userId != null) {
            Key<Account> accountKey = Key.create(Account.class, userId);

            for (Feed feed : query.list()) {
                if (LikeHelper.checkIsFeedLiked(feed.getKey(), accountKey)) {
                    feed.setLiked(true);
                }
            }
        }

        QueryResultIterator<Feed> queryIterator = query.iterator();
        List<Feed> feedList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            feedList.add(queryIterator.next());
        }
        return CollectionResponse.<Feed>builder()
                .setItems(feedList)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}