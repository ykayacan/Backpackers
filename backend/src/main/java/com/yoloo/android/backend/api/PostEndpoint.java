package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.controller.TimelineController;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AllowedToOperate;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

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
        resource = "timelinefeeds",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class
        }
)
public class PostEndpoint {

    /**
     * Log output.
     */
    private static final Logger logger =
            Logger.getLogger(PostEndpoint.class.getSimpleName());

    /**
     * Returns the {@link TimelinePost} with the corresponding ID.
     *
     * @param websafePostId the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Feed} with the provided ID.
     */
    @ApiMethod(
            name = "posts.get",
            path = "posts/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Post get(@Named("id") final String websafePostId,
                    final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(TimelinePost.class, websafePostId))
                .validate();

        return null;
    }

    /**
     * Inserts a new {@link TimelinePost}.
     */
    @ApiMethod(
            name = "posts.add",
            path = "posts",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Post add(@Named("content") final String content,
                    @Named("hashtags") final String hashtags,
                    @Named("location") final String location,
                    @Nullable @Named("mediaIds") final String mediaIds,
                    final HttpServletRequest request,
                    final User user)
            throws ServiceException {

        // TODO: 31.07.2016 Improve validation.
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return TimelineController.newInstance()
                .add(content, hashtags, location, mediaIds, request, user);
    }

    /**
     * Updates an existing {@code TimelinePost}.
     *
     * @param websafePostId the ID of the entity to be updated
     * @param request       the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing {@code
     *                           Question}
     */
    @ApiMethod(
            name = "posts.update",
            path = "posts/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Post update(@Named("id") final String websafePostId,
                       @Nullable @Named("content") final String content,
                       @Nullable @Named("hashtags") final String hashtags,
                       @Nullable @Named("location") final String location,
                       @Nullable @Named("mediaIds") final String mediaIds,
                       final HttpServletRequest request,
                       final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(TimelinePost.class, websafePostId))
                .addRule(new AllowedToOperate(TimelinePost.class, websafePostId, user, "update"))
                .validate();

        return TimelineController.newInstance()
                .update(websafePostId, content, hashtags, location, mediaIds, request, user);
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param websafePostId the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "posts.remove",
            path = "posts/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final String websafePostId,
                       final HttpServletRequest request,
                       final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(TimelinePost.class, websafePostId))
                .addRule(new AllowedToOperate(TimelinePost.class, websafePostId, user, "delete"))
                .validate();

        TimelineController.newInstance().remove(websafePostId, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "posts.list",
            path = "posts",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Post> list(@Nullable @Named("userId") final String websafeUserId,
                                         @Nullable @Named("cursor") final String cursor,
                                         @Nullable @Named("limit") Integer limit,
                                         final HttpServletRequest request,
                                         final User user)
            throws ServiceException {

        // TODO: 31.07.2016 Improve validation.
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return TimelineController.newInstance()
                .list(websafeUserId, cursor, limit, user);
    }
}