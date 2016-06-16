package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.exception.AlreadyFoundException;
import com.yoloo.android.backend.exception.InvalidIdException;
import com.yoloo.android.backend.exception.InvalidTokenException;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.util.EndpointUtil;
import com.yoloo.android.backend.util.LikeHelper;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.IdValidationRule;
import com.googlecode.objectify.Key;

import java.util.logging.Logger;

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
        resource = "likes",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class LikeEndpoint {

    private static final Logger logger = Logger.getLogger(LikeEndpoint.class.getName());

    /**
     * Adds a new {@code Like}.
     *
     * @param id          the id of the Feed
     * @param accessToken the access token
     * @throws InvalidTokenException the invalid token exception
     * @throws InvalidIdException    the invalid id exception
     * @throws AlreadyFoundException the already found exception
     */
    @ApiMethod(
            name = "likeFeed",
            path = "feeds/{id}/likes",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void likeFeed(@Named("id") final long id,
                         @Named("access_token") final String accessToken) throws ServiceException {

        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);

        EndpointUtil.checkItemExists(Feed.class, id);

        Key<Feed> feedKey = Key.create(Feed.class, id);

        LikeHelper.likeFeed(feedKey, accountKey);
    }

    /**
     * Deletes like from Feed.
     *
     * @param id          the id of the Feed
     * @param accessToken the access token
     * @throws InvalidTokenException the invalid token exception
     * @throws InvalidIdException    the invalid id exception
     * @throws NotFoundException     the not found exception
     */
    @ApiMethod(
            name = "unlikeFeed",
            path = "feeds/{id}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unlikeFeed(@Named("id") final long id,
                           @Named("access_token") final String accessToken) throws ServiceException {

        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);
        EndpointUtil.checkItemExists(Feed.class, id);

        Key<Feed> feedKey = Key.create(Feed.class, id);

        LikeHelper.unlikeFeed(feedKey, accountKey);
    }

    /**
     * Adds a new {@code Like}.
     *
     * @param id          the id of the Feed
     * @param accessToken the access token
     * @throws InvalidTokenException the invalid token exception
     * @throws InvalidIdException    the invalid id exception
     * @throws AlreadyFoundException the already found exception
     */
    @ApiMethod(
            name = "likeComment",
            path = "comments/{id}/likes",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void likeComment(@Named("id") final long id,
                            @Named("access_token") final String accessToken) throws ServiceException {

        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);

        EndpointUtil.checkItemExists(Comment.class, id);

        Key<Comment> commentKey = Key.create(Comment.class, id);

        LikeHelper.likeComment(commentKey, accountKey);
    }

    /**
     * Deletes like from Feed.
     *
     * @param id          the id of the Feed
     * @param accessToken the access token
     * @throws InvalidTokenException the invalid token exception
     * @throws InvalidIdException    the invalid id exception
     * @throws NotFoundException     the not found exception
     */
    @ApiMethod(
            name = "unlikeComment",
            path = "comments/{id}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unlikeComment(@Named("id") final long id,
                              @Named("access_token") final String accessToken) throws ServiceException {

        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);
        EndpointUtil.checkItemExists(Comment.class, id);

        Key<Comment> commentKey = Key.create(Comment.class, id);

        LikeHelper.unlikeComment(commentKey, accountKey);
    }
}