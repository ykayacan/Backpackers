package com.yoloo.android.backend.api;

import com.google.api.client.util.Strings;
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
import com.yoloo.android.backend.controller.ForumController;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.util.ClassUtil;
import com.yoloo.android.backend.util.StringUtil;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AllowedToOperate;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.question.QuestionCreateRule;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

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
     * Returns the {@link Question} with the corresponding ID.
     *
     * @param questionId the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Feed} with the provided ID.
     */
    @ApiMethod(
            name = "questions.get",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Question get(@Named("id") final String questionId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(questionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Question.class, questionId))
                .validate();

        return ForumController.newInstance().get(questionId, user);
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
                         @Nullable @Named("awardedBy") final String awardedBy,
                         @Nullable @Named("awardRep") final Integer awardRep,
                         final HttpServletRequest request,
                         final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new QuestionCreateRule(content, hashtags, locations))
                .addRule(new AuthenticationRule(user))
                .validate();

        return ForumController.newInstance()
                .add(content, hashtags, locations, mediaIds,
                        isLocked, awardedBy, awardRep, request, user);
    }

    /**
     * Updates an existing {@code Question}.
     *
     * @param questionId the ID of the entity to be updated
     * @param request    the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing {@code
     *                           Question}
     */
    @ApiMethod(
            name = "questions.update",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Question update(@Named("id") final String questionId,
                           @Nullable @Named("title") final String title,
                           @Nullable @Named("message") final String message,
                           @Nullable @Named("hashtag") final String hashtag,
                           @Nullable @Named("location") final String location,
                           @Nullable @Named("latlng") final String latLng,
                           final HttpServletRequest request,
                           final User user) throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(questionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Question.class, questionId))
                .validate();

        return null; /*QuestionController.newInstance()
                .update(questionId, title, message, hashtag, location, latLng, request, user);*/
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param questionId the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "questions.remove",
            path = "questions/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") final String questionId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(questionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(Question.class, questionId))
                .addRule(new AllowedToOperate(Question.class, questionId, user, "delete"))
                .validate();

        //QuestionHelper.remove(id);
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
    public CollectionResponse<Question> list(@Nullable @Named("user_id") final Long userId,
                                             @Nullable @Named("sort") final String sort,
                                             @Nullable @Named("cursor") final String cursor,
                                             @Nullable @Named("limit") Integer limit,
                                             @Nullable @Named("hashtag") final String hashtag,
                                             final User user) throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Question> query = ofy().load().type(Question.class);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        if (sort != null) {
            // split sort options into String array. Also ignores whitespaces in both side.
            final List<String> options = StringUtil.splitValueByComma(sort);
            for (String option : options) {
                if (option.equals("created_at")) {
                    query = query.order("createdAt");
                }
                if (option.equals("-created_at")) {
                    query = query.order("-createdAt");
                }
                if (option.equals("like")) {
                    query = query.order("likesCount");
                }
                if (option.equals("-like")) {
                    query = query.order("-likesCount");
                }
            }
        }

        if (!Strings.isNullOrEmpty(hashtag)) {
            query = query.filter("hashtags =", hashtag);
        }

        query = query.limit(limit);

        List<Like<Question>> likes = ofy().load()
                .type(ClassUtil.<Like<Question>>castClass(Like.class))
                .ancestor(Key.create(user.getUserId())).project("question").list();

        QueryResultIterator<Question> queryIterator = query.iterator();
        List<Question> questionList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            questionList.add(queryIterator.next());
        }

        for (Like<Question> like : likes) {
            for (Question q : questionList) {
                if (like.getLikeableEntityKey().equivalent(q.getKey())) {
                    q.setLiked(true);
                }
            }
        }

        return CollectionResponse.<Question>builder()
                .setItems(questionList)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}