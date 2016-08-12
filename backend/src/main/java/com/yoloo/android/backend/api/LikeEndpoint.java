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
import com.yoloo.android.backend.controller.LikeManager;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.like.LikeConflictRule;
import com.yoloo.android.backend.validator.rule.like.LikeNotFoundRule;
import com.yoloo.android.backend.validator.rule.like.PostLikeConflictRule;
import com.yoloo.android.backend.validator.rule.like.PostLikeNotFoundRule;

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

    private static final Logger logger = Logger.getLogger(LikeEndpoint.class.getSimpleName());

    /**
     * Adds a new {@code Like}.
     *
     * @param websafePostId the id of the Feed
     * @param user       the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "posts.like",
            path = "posts/{post_id}/likes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void likePost(@Named("post_id") final String websafePostId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(TimelinePost.class, websafePostId))
                .addRule(new PostLikeConflictRule(websafePostId, user))
                .validate();

        LikeController.newInstance(user, websafePostId).like();
    }

    /**
     * Deletes like from Feed.
     *
     * @param websafePostId the id of the Post
     * @param user       the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "posts.dislike",
            path = "posts/{post_id}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void dislikePost(@Named("post_id") final String websafePostId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(TimelinePost.class, websafePostId))
                .addRule(new PostLikeNotFoundRule(websafePostId, user))
                .validate();

        LikeController.newInstance(user, websafePostId).dislike();
    }

    /**
     * Adds a new {@code Like}.
     *
     * @param websafeCommentId the id of the Question
     * @param user      the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "comments.like",
            path = "comments/{comment_id}/likes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void likeComment(@Named("comment_id") final String websafeCommentId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeCommentId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Comment.class, websafeCommentId))
                .addRule(new LikeConflictRule(websafeCommentId, user))
                .validate();

        LikeManager.newInstance(Comment.class, websafeCommentId, user).like();
    }

    /**
     * Deletes like from Feed.
     *
     * @param commentId the id of the Feed
     * @param user      the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "comments.unlike",
            path = "comments/{comment_id}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unlikeComment(@Named("comment_id") final String commentId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(commentId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Comment.class, commentId))
                .addRule(new LikeNotFoundRule(commentId, user))
                .validate();

        LikeManager.newInstance(Comment.class, commentId, user).unlike();
    }

    /**
     * Adds a new {@code Like}.
     *
     * @param questionId the id of the Feed
     * @param user       the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "questions.like",
            path = "questions/{question_id}/likes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void likeQuestion(@Named("question_id") final String questionId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(questionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Question.class, questionId))
                .addRule(new LikeConflictRule(questionId, user))
                .validate();

        LikeManager.newInstance(Question.class, questionId, user).like();
    }

    /**
     * Deletes like from Feed.
     *
     * @param questionId the id of the Question
     * @param user       the parentUserKey
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "questions.dislike",
            path = "questions/{question_id}/likes",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void dislikeQuestion(@Named("question_id") final String questionId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(questionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Question.class, questionId))
                .addRule(new LikeNotFoundRule(questionId, user))
                .validate();

        LikeManager.newInstance(Question.class, questionId, user).unlike();
    }
}