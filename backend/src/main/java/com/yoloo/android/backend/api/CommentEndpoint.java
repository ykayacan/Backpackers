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
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.util.CommentHelper;
import com.yoloo.android.backend.util.EndpointUtil;
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
        resource = "comments",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class CommentEndpoint {

    private static final Logger logger = Logger.getLogger(CommentEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Inserts a new {@code Comment}.
     */
    @ApiMethod(
            name = "addComment",
            path = "feeds/{id}/comments",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Comment add(@Named("id") final long id,
                       @Named("text") final String text,
                       @Named("access_token") final String accessToken) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(id));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);

        EndpointUtil.checkItemExists(Feed.class, id);

        return CommentHelper.createComment(accountKey, Key.create(Feed.class, id), text);
    }

    /**
     * Deletes the specified {@code Comment}.
     *
     * @param feedId the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Comment}
     */
    @ApiMethod(
            name = "removeComment",
            path = "feeds/{feed_id}/comments/{comment_id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("feed_id") final long feedId,
                       @Named("comment_id") final long commentId,
                       @Named("access_token") String accessToken) throws ServiceException {

        // Validate.
        Validator validator = Validator.get();
        validator.addRule(new IdValidationRule(feedId));
        validator.addRule(new IdValidationRule(commentId));
        validator.validate();

        Key<Account> accountKey = EndpointUtil.isValidToken(accessToken);
        EndpointUtil.checkItemExists(Comment.class, commentId);
        EndpointUtil.checkIsAuthorizedToRemove(Comment.class, commentId, accountKey);

        CommentHelper.removeComment(commentId);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listComment",
            path = "feeds/{id}/comments",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Comment> list(@Named("id") final long id,
                                            @Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Comment> query = ofy().load().type(Comment.class);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.filter("feedRef =", Key.create(Feed.class, id));
        query = query.limit(limit);

        QueryResultIterator<Comment> queryIterator = query.iterator();
        List<Comment> commentList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            commentList.add(queryIterator.next());
        }
        return CollectionResponse.<Comment>builder()
                .setItems(commentList)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}