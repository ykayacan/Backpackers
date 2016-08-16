package com.yoloo.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.factory.post.NormalPostFactory;
import com.yoloo.android.backend.factory.post.PostFactory;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.AbstractPost;
import com.yoloo.android.backend.model.feed.post.NormalPost;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.servlet.RemoveTimelineServlet;
import com.yoloo.android.backend.util.CommentHelper;
import com.yoloo.android.backend.util.LikeHelper;
import com.yoloo.android.backend.util.TimelineUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class TimelineController extends PostController {

    private static final Logger logger =
            Logger.getLogger(TimelineController.class.getName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * New instance timeline feed controller.
     *
     * @return the timeline feed controller
     */
    public static TimelineController newInstance() {
        return new TimelineController();
    }

    /**
     * Add timeline post.
     *
     * @param content  the content
     * @param hashtags the hashtags
     * @param location the location
     * @param mediaIds the media ids
     * @param request  the request
     * @param user     the user
     * @return the timeline post
     */
    public AbstractPost add(final String content,
                    final String hashtags,
                    final String location,
                    final String mediaIds,
                    final HttpServletRequest request,
                    final User user) {
        // Create parent user key.
        final Key<Account> userKey = Key.create(user.getUserId());
        final LoadResult<Key<Follow>> followResult = ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().first();

        // Get related account.
        final Account account = ofy().load().key(userKey).now();

        // Allocate an id with parent user key.
        final Key<NormalPost> postKey =
                ofy().factory().allocateId(userKey, NormalPost.class);

        // TODO: 27.07.2016 Get all media links related to media ids.

        // Create a new post.
        final NormalPost post = (NormalPost) PostFactory.getPost(
                new NormalPostFactory(postKey, account, content, hashtags, location));

        // Add current post to user's feed.
        final TimelineFeed feed =
                TimelineFeed.newInstance(userKey, postKey, post.getCreatedAt());

        // Batch save entities
        ofy().save().entities(ImmutableList.builder()
                .add(post)
                .add(feed)
                .addAll(post.getLocations())
                .build());

        TimelineUtil.updateTimeline(userKey, followResult, post);

        return post;
    }

    /**
     * Update timeline post.
     *
     * @param websafePostId the websafe post id
     * @param content       the content
     * @param hashtags      the hashtags
     * @param location      the location
     * @param mediaIds      the media ids
     * @param request       the request
     * @param user          the user
     * @return the timeline post
     */
    public AbstractPost update(final String websafePostId,
                       final String content,
                       final String hashtags,
                       final String location,
                       final String mediaIds,
                       final HttpServletRequest request,
                       final User user) {
        final Key<NormalPost> postKey = Key.create(websafePostId);
        final Key<Account> userKey = Key.create(user.getUserId());

        NormalPost post = ofy().load().key(postKey).now();

        final LoadResult<Key<Like>> asyncLikesResult =
                LikeHelper.loadAsyncPostLikes(userKey, postKey);
        final LoadResult<Key<Comment>> asyncCommentsResult =
                CommentHelper.loadAsyncComments(userKey, postKey);

        // TODO: 10.08.2016 Add mediaId case.

        updateContent(post, content);
        updateHashtags(post, hashtags);
        updateLocations(post, location);
        updateDate(post);

        LikeHelper.aggregateLike(post, asyncLikesResult);
        CommentHelper.aggregateComments(post, asyncCommentsResult);

        ofy().save().entities(ImmutableList.builder()
                .add(post)
                .addAll(post.getLocations())
                .build());

        return post;
    }

    /**
     * Remove.
     *
     * @param websafePostId the websafe post id
     * @param user          the user
     */
    public void remove(final String websafePostId,
                       final User user) {
        // Get Post key.
        RemoveTimelineServlet.create(websafePostId);
    }

    public CollectionResponse<AbstractPost> list(final String websafeUserId,
                                                 final String cursor,
                                                 Integer limit,
                                                 final User user) {

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(user.getUserId());

        final Key<Account> parentUserKey = websafeUserId == null
                ? userKey
                : Key.<Account>create(websafeUserId);

        Query<TimelineFeed> query = ofy().load().type(TimelineFeed.class)
                .ancestor(parentUserKey);

        query = buildQuery(cursor, limit, query);

        final QueryResultIterator<TimelineFeed> queryIterator = query.iterator();

        // Store async batch in a hashmap. LinkedHashMap preserve insertion order.
        final Map<Key<AbstractPost>, LoadResult<Key<Like>>> likeKeysMap =
                new LinkedHashMap<>(limit);
        final Map<Key<? extends AbstractPost>, LoadResult<Key<Comment>>> commentKeysMap =
                new LinkedHashMap<>(limit);

        while (queryIterator.hasNext()) {
            // Get post key.
            final Key<AbstractPost> postKey = queryIterator.next().getPostKey();

            likeKeysMap.put(postKey, LikeHelper.loadAsyncPostLikes(userKey, postKey));
            commentKeysMap.put(postKey, CommentHelper.loadAsyncComments(userKey, postKey));
        }

        // Batch load all post keys for timeline.
        final Collection<AbstractPost> abstractPosts =
                ofy().load().keys(likeKeysMap.keySet()).values();

        LikeHelper.aggregatePostLikes(abstractPosts, likeKeysMap);
        CommentHelper.aggregateComments(abstractPosts, commentKeysMap);

        return CollectionResponse.<AbstractPost>builder()
                .setItems(abstractPosts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    private Query<TimelineFeed> buildQuery(String cursor, Integer limit,
                                           Query<TimelineFeed> query) {
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.order("-createdAt");
        query = query.limit(limit);

        return query;
    }
}
