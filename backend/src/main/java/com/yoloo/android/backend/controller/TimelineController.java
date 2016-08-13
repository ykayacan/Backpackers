package com.yoloo.android.backend.controller;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.PostFactory;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.servlet.CreateTimelineServlet;
import com.yoloo.android.backend.servlet.RemoveTimelineServlet;
import com.yoloo.android.backend.util.LikeHelper;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class TimelineController {

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
    public Post add(final String content,
                    final String hashtags,
                    final String location,
                    final String mediaIds,
                    final HttpServletRequest request,
                    final User user) {
        // Create parent user key.
        final Key<Account> userKey = Key.create(user.getUserId());

        // Get related account.
        final Account account = ofy().load().key(userKey).now();

        // Allocate an id with parent user key.
        final Key<TimelinePost> postKey =
                ofy().factory().allocateId(userKey, TimelinePost.class);

        // TODO: 27.07.2016 Get all media links related to media ids.

        // Create a new post.
        final TimelinePost post = new PostFactory()
                .createTimelinePost(postKey, account, content, hashtags, location);

        // Add current post to user's feed.
        final TimelineFeed feed =
                TimelineFeed.newInstance(userKey, postKey, post.getCreatedAt());

        writeToFollowersTimeline(userKey, postKey, post);

        // Add all entities to an immutable list.
        List<Object> saveList = ImmutableList.builder()
                .add(post)
                .add(feed)
                .addAll(post.getLocations())
                .build();

        // Batch save entities
        ofy().save().entities(saveList);

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
    public Post update(final String websafePostId,
                       final String content,
                       final String hashtags,
                       final String location,
                       final String mediaIds,
                       final HttpServletRequest request,
                       final User user) {
        final Key<TimelinePost> postKey = Key.create(websafePostId);
        final Key<Account> userKey = Key.create(user.getUserId());

        TimelinePost post = ofy().load().key(postKey).now();

        final LoadResult<Key<Like>> result = LikeHelper.loadAsyncLikes(userKey, postKey);

        List<Object> saveList = new ArrayList<>(2);

        // Check if any part of the entity is changed.
        boolean isUpdated;

        // TODO: 10.08.2016 Add mediaId case.

        isUpdated = updateContent(content, post, false);
        isUpdated = updateHashtags(hashtags, post, isUpdated);
        isUpdated = updateLocations(location, postKey, post, saveList, isUpdated);
        updateDate(post, isUpdated);

        if (isUpdated) {
            post.setUpdatedAt(new Date());
        }

        saveList.add(post);

        ofy().save().entities(saveList);

        LikeHelper.aggregateLikes(post, result);

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

    /**
     * List collection response.
     *
     * @param websafeUserId the websafe user key
     * @param cursor        the cursor
     * @param limit         the limit
     * @param user          the user
     * @return the collection response
     * @throws ServiceException the service exception
     */
    public CollectionResponse<Post> list(final String websafeUserId,
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
        final Map<Key<Post>, LoadResult<Key<Like>>> map = new LinkedHashMap<>(limit);

        while (queryIterator.hasNext()) {
            // Get post key.
            final Key<Post> postKey = queryIterator.next().getPostKey();
            final LoadResult<Key<Like>> result = LikeHelper.loadAsyncLikes(userKey, postKey);

            map.put(postKey, result);
        }

        // Batch load all post keys for timeline.
        final Collection<Post> posts = ofy().load().keys(map.keySet()).values();

        LikeHelper.aggregateLikes(posts, map);

        return CollectionResponse.<Post>builder()
                .setItems(posts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    private void updateDate(TimelinePost post, boolean isUpdated) {
        if (isUpdated) {
            post.setUpdatedAt(new Date());
        }
    }

    private boolean updateLocations(String locations, Key<TimelinePost> postKey, TimelinePost post,
                                    List<Object> saveList, boolean isUpdated) {
        if (!Strings.isNullOrEmpty(locations)) {
            // Find related Location entities with the given post key.
            List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                    .filter("postKey =", postKey).keys().list();

            // Delete all entities.
            ofy().delete().keys(locationKeys);

            // Generate locations from given string with immutable way.
            Set<Location> locationSet =
                    ImmutableSet.copyOf(LocationHelper.getLocationList(locations, postKey));
            post.setLocations(locationSet);

            saveList.addAll(locationSet);

            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateHashtags(String hashtags, TimelinePost post, boolean isUpdated) {
        if (!Strings.isNullOrEmpty(hashtags)) {
            post.getHashtags().clear();
            post.getHashtags().addAll(StringUtil.splitValueByToken(hashtags, ","));
            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateContent(String content, TimelinePost post, boolean isUpdated) {
        if (!Strings.isNullOrEmpty(content)) {
            post.setContent(content);
            isUpdated = true;
        }
        return isUpdated;
    }

    private void writeToFollowersTimeline(Key<Account> userKey,
                                          Key<TimelinePost> postKey,
                                          TimelinePost post) {
        final Key<Follow> followKey = ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().first().now();

        // The user is followed by someone.
        if (followKey != null) {
            // Write post to user's followers timeline.
            CreateTimelineServlet.create(
                    userKey.toWebSafeString(),
                    postKey.toWebSafeString(),
                    String.valueOf(post.getCreatedAt().getTime()));
        }
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
