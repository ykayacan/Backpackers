package com.backpackers.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.model.hashtag.HashTag;
import com.backpackers.android.backend.util.LocationHelper;
import com.backpackers.android.backend.validator.Validator;
import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.authenticator.FacebookAuthenticator;
import com.backpackers.android.backend.authenticator.GoogleAuthenticator;
import com.backpackers.android.backend.authenticator.YolooAuthenticator;
import com.backpackers.android.backend.controller.SearchController;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.location.LocationInfo;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.validator.rule.common.AuthenticationRule;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

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
        resource = "searches",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID,},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class
        }
)
public class SearchEndpoint {

    private static final Logger logger =
            Logger.getLogger(SearchEndpoint.class.getName());

    /**
     * Maximum number of places to return.
     */
    private static final int MAXIMUM_NUMBER_PLACES = 100;

    /**
     * Maximum distance to search places to return.
     */
    private static final int MAXIMUM_DISTANCE = 100;

    /**
     * The number of meters in a kilometer.
     */
    private static final int METERS_IN_KILOMETER = 1000;

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "search.users.list",
            path = "search/users",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> listUsers(@Named("q") final String queryText,
                                                 @Nullable @Named("cursor") final String cursor,
                                                 @Nullable @Named("limit") Integer limit,
                                                 final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return SearchController.newInstance().searchUsers(cursor, limit, queryText, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "search.hashtags.list",
            path = "search/hashtags",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<HashTag> listHashTags(@Named("q") final String queryText,
                                                    @Nullable @Named("cursor") final String cursor,
                                                    @Nullable @Named("limit") Integer limit,
                                                    final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return SearchController.newInstance().searchHashTags(cursor, limit, queryText, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "search.posts.list",
            path = "search/posts",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<ForumPost> listPosts(@Named("q") final String queryText,
                                                   @Nullable @Named("cursor") final String cursor,
                                                   @Nullable @Named("limit") Integer limit,
                                                   final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return SearchController.newInstance().searchPosts(cursor, limit, queryText, user);
    }

    /**
     * Lists nearby places.
     *
     * @param longitudeString the location longitude.
     * @param latitudeString  the location latitude.
     * @param pDistanceInKm   the maximum distance to search for nearby places.
     * @param pCount          the maximum number of places returned.
     * @param user            the user that requested the entities.
     * @return List of nearby places.
     * @throws com.google.api.server.spi.ServiceException if user is not
     *                                                    authorized
     */
    @ApiMethod(
            name = "search.locations.list",
            path = "search/locations",
            httpMethod = ApiMethod.HttpMethod.GET)
    public final List<LocationInfo> listPlaces(@Named("longitude") final String longitudeString,
                                               @Named("latitude") final String latitudeString,
                                               @Named("distanceInKm") final long pDistanceInKm,
                                               @Named("count") final int pCount, final User user) throws
            ServiceException {

        float latitude;
        float longitude;
        GeoPt location;
        int count = pCount;
        long distanceInKm = pDistanceInKm;

        try {
            latitude = (float) Double.parseDouble(latitudeString);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid value of 'latitude' argument");
        }

        try {
            longitude = (float) Double.parseDouble(longitudeString);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid value of 'longitude' argument");
        }

        try {
            location = new GeoPt(latitude, longitude);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid pair of 'latitude' and 'longitude' arguments");
        }

        // limit the result set to up to MAXIMUM_NUMBER_PLACES places within
        // up to MAXIMUM_DISTANCE km
        if (count > MAXIMUM_NUMBER_PLACES) {
            count = MAXIMUM_NUMBER_PLACES;
        } else if (count <= 0) {
            throw new BadRequestException("Invalid value of 'count' argument");
        }

        if (distanceInKm > MAXIMUM_DISTANCE) {
            distanceInKm = MAXIMUM_DISTANCE;
        } else if (distanceInKm < 0) {
            throw new BadRequestException(
                    "Invalid value of 'distanceInKm' argument");
        }

        return LocationHelper
                .getPlaces(location, METERS_IN_KILOMETER * distanceInKm, count);
    }
}
