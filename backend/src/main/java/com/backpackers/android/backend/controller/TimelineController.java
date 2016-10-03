package com.backpackers.android.backend.controller;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.backpackers.android.backend.factory.post.NormalPostFactory;
import com.backpackers.android.backend.factory.post.PostFactory;
import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.feed.TimelineFeed;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.feed.post.NormalPost;
import com.backpackers.android.backend.model.like.Like;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.servlet.RemoveTimelineServlet;
import com.backpackers.android.backend.util.CommentHelper;
import com.backpackers.android.backend.util.LikeHelper;
import com.backpackers.android.backend.util.StringUtil;
import com.backpackers.android.backend.util.TimelineUtil;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.model.follow.Follow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

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
        final LoadResult<Key<Follow>> followResult = OfyHelper.ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().first();

        Map<Key<Media>, Media> mediaMap = null;
        if (!Strings.isNullOrEmpty(mediaIds)) {
            List<Key<Media>> mediaKeys = new ArrayList<>(5);
            List<String> mediaIdList = StringUtil.splitValueByToken(mediaIds, ",");

            for (String mediaId : mediaIdList) {
                mediaKeys.add(Key.<Media>create(mediaId));
            }
            mediaMap = OfyHelper.ofy().load().keys(mediaKeys);
        }

        // Get related account.
        final Account account = OfyHelper.ofy().load().key(userKey).now();

        // Allocate an id with parent user key.
        final Key<NormalPost> postKey =
                OfyHelper.ofy().factory().allocateId(userKey, NormalPost.class);

        // TODO: 27.07.2016 Get all media links related to media ids.

        // Create a new post.
        final NormalPost post = (NormalPost) PostFactory.create(
                new NormalPostFactory(postKey, account, content, hashtags, location, mediaMap));

        // Add current post to user's feed.
        final TimelineFeed feed =
                TimelineFeed.newInstance(userKey, postKey, post.getCreatedAt());

        // Batch save entities
        OfyHelper.ofy().save().entities(ImmutableList.builder()
                .add(post)
                .add(feed)
                .add(post.getLocation())
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

        NormalPost post = OfyHelper.ofy().load().key(postKey).now();

        final LoadResult<Key<Like>> asyncLikesResult =
                LikeHelper.loadAsyncPostLikes(userKey, postKey);
        final LoadResult<Key<Comment>> asyncCommentsResult =
                CommentHelper.loadAsyncComments(userKey, postKey);

        // TODO: 10.08.2016 Add mediaId case.

        updateContent(post, content);
        updateHashTags(post, hashtags);
        updateLocation(post, location);
        updateDate(post);

        LikeHelper.aggregateLike(post, asyncLikesResult);
        CommentHelper.aggregateComments(post, asyncCommentsResult);

        OfyHelper.ofy().save().entities(ImmutableList.builder()
                .add(post)
                .add(post.getLocation())
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

        Query<TimelineFeed> query = OfyHelper.ofy().load().type(TimelineFeed.class)
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
                OfyHelper.ofy().load().keys(likeKeysMap.keySet()).values();

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
