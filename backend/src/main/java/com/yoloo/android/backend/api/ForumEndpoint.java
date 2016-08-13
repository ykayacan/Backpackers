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
import com.yoloo.android.backend.controller.ForumController;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AllowedToOperate;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.question.QuestionCreateRule;

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
        resource = "questions",
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
public class ForumEndpoint {

    /**
     * Log output.
     */
    private static final Logger logger =
            Logger.getLogger(ForumEndpoint.class.getSimpleName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Returns the {@link ForumPost} with the corresponding ID.
     *
     * @param websafePostId the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Feed} with the provided ID.
     */
    @ApiMethod(
            name = "questions.get",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public ForumPost get(@Named("id") final String websafePostId,
                         final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .validate();

        return ForumController.newInstance().get(websafePostId, user);
    }

    /**
     * Inserts a new {@code Question}.
     */
    @ApiMethod(
            name = "questions.add",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.POST)
    public ForumPost add(@Named("content") final String content,
                         @Named("hashtags") final String hashtags,
                         @Named("locations") final String locations,
                         @Nullable @Named("mediaIds") final String mediaIds,
                         @Nullable @Named("isLocked") final Boolean isLocked,
                         @Nullable @Named("awardRep") final Integer awardRep,
                         final HttpServletRequest request,
                         final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new QuestionCreateRule(content, hashtags, locations))
                .addRule(new AuthenticationRule(user))
                .validate();

        return ForumController.newInstance()
                .add(content, hashtags, locations, mediaIds, isLocked, awardRep, request, user);
    }

    /**
     * Updates an existing {@code Question}.
     *
     * @param websafePostId the ID of the entity to be updated
     * @param request       the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing {@code
     *                           Question}
     */
    @ApiMethod(
            name = "questions.update",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public ForumPost update(@Named("id") final String websafePostId,
                            @Nullable @Named("content") final String content,
                            @Nullable @Named("hashtags") final String hashtags,
                            @Nullable @Named("locations") final String locations,
                            @Nullable @Named("mediaIds") final String mediaIds,
                            @Nullable @Named("isLocked") final Boolean isLocked,
                            @Nullable @Named("awardRep") final Integer awardRep,
                            @Nullable @Named("isAccepted") final Boolean isAccepted,
                            final HttpServletRequest request,
                            final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .addRule(new AllowedToOperate(user, websafePostId, AllowedToOperate.Operation.UPDATE))
                .validate();

        return ForumController.newInstance()
                .update(websafePostId, content, hashtags, locations, mediaIds, isLocked,
                        awardRep, isAccepted, request, user);
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param websafePostId the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "questions.remove",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final String websafePostId,
                       final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .addRule(new AllowedToOperate(user, websafePostId, AllowedToOperate.Operation.DELETE))
                .validate();

        ForumController.newInstance().remove(websafePostId, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "questions.list",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<ForumPost> list(@Nullable @Named("sort") final String sort,
                                              @Nullable @Named("cursor") final String cursor,
                                              @Nullable @Named("limit") Integer limit,
                                              final User user) throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return ForumController.newInstance().list(sort, cursor, limit, user);
    }
}