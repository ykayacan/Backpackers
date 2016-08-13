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
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserCounterShard;
import com.yoloo.android.backend.model.vote.Vote;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;
import com.yoloo.android.backend.util.VoteHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class ForumController {

    private static final Logger logger =
            Logger.getLogger(ForumController.class.getName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    private static final String SORT_BY_NEWEST = "newest";
    private static final String SORT_BY_HOT = "hot";


    public static ForumController newInstance() {
        return new ForumController();
    }

    public ForumPost get(final String websafePostKey, final User user) {
        // The key of the post.
        Key<ForumPost> postKey = Key.create(websafePostKey);

        ForumPost post = ofy().load().key(postKey).now();

        return post;
    }

    /**
     * Creates a new Question with given parameters.
     *
     * @return the Question
     */
    public ForumPost add(final String content,
                         final String hashtags,
                         final String locations,
                         final String mediaIds,
                         final Boolean isLocked,
                         final Integer awardRep,
                         final HttpServletRequest request,
                         final User user) {
        // Create parent user key.
        final Key<Account> userKey = Key.create(user.getUserId());

        // Get related account.
        final Account account = ofy().load().key(userKey).now();

        // Allocate an id with parent user key.
        final Key<ForumPost> postKey =
                ofy().factory().allocateId(userKey, ForumPost.class);

        // TODO: 12.08.2016 Gamification here.

        // Create a new post.
        final ForumPost post = new PostFactory()
                .createForumPost(postKey, account, content, hashtags,
                        locations, isLocked, awardRep);

        // Increase question counter of the current user.
        final UserCounterShard counter = loadAndIncreaseUserQuestionCounter(userKey);

        // Add all entities to an immutable list.
        List<Object> saveList = ImmutableList.builder()
                .add(post)
                .add(counter)
                .addAll(post.getLocations())
                .build();

        // Batch save entities
        ofy().save().entities(saveList);

        return post;
    }

    public ForumPost update(final String websafePostId,
                            final String content,
                            final String hashtags,
                            final String locations,
                            final String mediaIds,
                            final Boolean isLocked,
                            final Integer awardRep,
                            final Boolean isAccepted,
                            final HttpServletRequest request,
                            final User user) {
        final Key<ForumPost> postKey = Key.create(websafePostId);
        final Key<Account> userKey = Key.create(user.getUserId());

        final ForumPost post = ofy().load().key(postKey).now();

        ImmutableList<LoadResult<Key<Vote>>> loadResults =
                VoteHelper.loadAsyncVoteKeys(userKey, postKey);

        List<Object> saveList = new ArrayList<>(2);

        // Check if any part of the entity is changed.
        boolean isUpdated;

        // TODO: 10.08.2016 Add mediaId case.

        isUpdated = updateContent(content, post, false);
        isUpdated = updateHashtags(hashtags, post, isUpdated);
        isUpdated = updateLocations(locations, postKey, post, saveList, isUpdated);
        isUpdated = updateLocked(isLocked, post, isUpdated);
        isUpdated = updateAward(awardRep, userKey, post, isUpdated);
        isUpdated = updateAccepted(isAccepted, post, isUpdated);
        updateDate(post, isUpdated);

        saveList.add(post);

        VoteHelper.aggregateVotes(post, loadResults);

        ofy().save().entities(saveList);

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
        Key<ForumPost> postKey = Key.create(websafePostId);
        Key<Account> userKey = Key.create(user.getUserId());

        List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().list();

        List<Key<Vote>> voteKeys = ofy().load().type(Vote.class)
                .filter("postKey =", postKey)
                .keys().list();

        final UserCounterShard shard = ofy().load().type(UserCounterShard.class)
                .ancestor(userKey).first().now();

        shard.setQuestionCount(shard.getQuestionCount() - 1);

        List<Object> deleteList = ImmutableList.builder()
                .addAll(locationKeys)
                .addAll(voteKeys)
                .add(postKey)
                .build();

        ofy().delete().entities(deleteList);
        ofy().save().entity(shard);
    }

    /**
     * List collection response.
     *
     * @param cursor the cursor
     * @param limit  the limit
     * @param user   the user
     * @return the collection response
     * @throws ServiceException the service exception
     */
    public CollectionResponse<ForumPost> list(final String sort,
                                              final String cursor,
                                              Integer limit,
                                              final User user) {

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(user.getUserId());

        Query<ForumPost> query = ofy().load().type(ForumPost.class);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        if (sort != null) {
            if (sort.equals(SORT_BY_NEWEST)) {
                query = query.order("-createdAt");
            }
        }

        query = query.limit(limit);

        final QueryResultIterator<ForumPost> queryIterator = query.iterator();

        // Store async batch in a hashmap. LinkedHashMap preserve insertion order.
        final Map<Key<ForumPost>, List<LoadResult<Key<Vote>>>> votedKeysMap =
                new LinkedHashMap<>(limit);
        final List<ForumPost> posts = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            // Get post key.
            final ForumPost post = queryIterator.next();
            posts.add(post);
            votedKeysMap.put(post.getKey(),
                    VoteHelper.loadAsyncVoteKeys(userKey, post.getKey()));
        }

        VoteHelper.aggregateVotes(posts, votedKeysMap);

        return CollectionResponse.<ForumPost>builder()
                .setItems(posts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    private void updateDate(ForumPost post, boolean isUpdated) {
        if (isUpdated) {
            post.setUpdatedAt(new Date());
        }
    }

    private boolean updateAccepted(Boolean isAccepted, ForumPost post, boolean isUpdated) {
        if (isAccepted != null) {
            post.setAccepted(isAccepted);
            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateAward(Integer awardRep, Key<Account> userKey,
                                ForumPost post, boolean isUpdated) {
        if (awardRep != null) {
            post.setAwardedBy(userKey);
            post.setAwardRep(awardRep);
            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateLocked(Boolean isLocked, ForumPost post, boolean isUpdated) {
        if (isLocked != null) {
            post.setLocked(isLocked);
            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateLocations(String locations, Key<ForumPost> postKey, ForumPost post,
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

    private boolean updateHashtags(String hashtags, ForumPost post, boolean isUpdated) {
        if (!Strings.isNullOrEmpty(hashtags)) {
            post.getHashtags().clear();
            post.getHashtags().addAll(StringUtil.splitValueByToken(hashtags, ","));
            isUpdated = true;
        }
        return isUpdated;
    }

    private boolean updateContent(String content, ForumPost post, boolean isUpdated) {
        if (!Strings.isNullOrEmpty(content)) {
            post.setContent(content);
            isUpdated = true;
        }
        return isUpdated;
    }

    private UserCounterShard loadAndIncreaseUserQuestionCounter(Key<Account> userKey) {
        // Find first shard and increase the question counter.
        // Users don't post a question rapidly so it is safe to use first shard.
        UserCounterShard shard = ofy().load().type(UserCounterShard.class)
                .ancestor(userKey).first().now();

        shard.setQuestionCount(shard.getQuestionCount() + 1);
        return shard;
    }
}
