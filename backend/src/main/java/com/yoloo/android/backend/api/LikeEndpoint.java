package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.controller.LikeController;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.like.LikeConflictRule;
import com.yoloo.android.backend.validator.rule.like.LikeNotFoundRule;

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
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class}
)
public class LikeEndpoint {

    private static final Logger logger =
            Logger.getLogger(LikeEndpoint.class.getName());

    /**
     * Adds a new {@code Like}.
     *
     * @param websafePostId the id of the Feed
     * @param user          the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "posts.like",
            path = "posts/{postId}/likes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void likePost(@Named("postId") final String websafePostId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .addRule(new LikeConflictRule(websafePostId, user))
                .validate();

        LikeController.newInstance(user, websafePostId).like();
    }

    /**
     * Deletes like from Feed.
     *
     * @param websafePostId the id of the Post
     * @param user          the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "posts.dislike",
            path = "posts/{postId}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void dislikePost(@Named("postId") final String websafePostId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .addRule(new LikeNotFoundRule(websafePostId, user))
                .validate();

        LikeController.newInstance(user, websafePostId).dislike();
    }

    /**
     * Adds a new {@code Like}.
     *
     * @param websafeCommentId the id of the Question
     * @param user             the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "comments.like",
            path = "comments/{commentId}/likes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void likeComment(@Named("commentId") final String websafeCommentId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeCommentId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeCommentId))
                .addRule(new LikeConflictRule(websafeCommentId, user))
                .validate();

        LikeController.newInstance(user, websafeCommentId).like();
    }

    /**
     * Deletes like from Feed.
     *
     * @param websafeCommentId the id of the Feed
     * @param user             the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "comments.dislike",
            path = "comments/{commentId}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void dislikeComment(@Named("commentId") final String websafeCommentId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeCommentId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeCommentId))
                .addRule(new LikeNotFoundRule(websafeCommentId, user))
                .validate();

        LikeController.newInstance(user, websafeCommentId).dislike();
    }
}