package com.yoloo.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.factory.ForumPostFactory;
import com.yoloo.android.backend.factory.PostFactory;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserCounterShard;
import com.yoloo.android.backend.model.vote.Vote;
import com.yoloo.android.backend.util.CommentHelper;
import com.yoloo.android.backend.util.TimelineUtil;
import com.yoloo.android.backend.util.VoteHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class ForumController extends PostController {

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
        final LoadResult<Key<Follow>> followResult = ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().first();

        // Get related account.
        final Account account = ofy().load().key(userKey).now();

        // Allocate an id with parent user key.
        final Key<? extends Post> postKey =
                ofy().factory().allocateId(userKey, ForumPost.class);

        // Create a new post.
        final ForumPost post = (ForumPost) PostFactory.getPost(
                new ForumPostFactory(
                        postKey, account, content, hashtags,
                        locations, isLocked, awardRep));

        // TODO: 12.08.2016 Gamification here.

        // Increase question counter of the current user.
        final UserCounterShard shard = loadAndIncreaseUserQuestionCounter(userKey);

        // Batch save entities
        ofy().save().entities(ImmutableList.builder()
                .add(post)
                .add(shard)
                .addAll(post.getLocations())
                .build());

        TimelineUtil.updateTimeline(userKey, followResult, post);

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

        ImmutableList<LoadResult<Key<Vote>>> asyncVoteResult =
                VoteHelper.loadAsyncVoteKeys(userKey, postKey);
        final LoadResult<Key<Comment>> asyncCommentsResult =
                CommentHelper.loadAsyncComments(userKey, postKey);

        // TODO: 10.08.2016 Add mediaId case.

        updateContent(post, content);
        updateHashtags(post, hashtags);
        updateLocations(post, locations);
        updateLocked(post, isLocked);
        updateAward(post, userKey, awardRep);
        updateAccepted(post, isAccepted);
        updateDate(post);

        VoteHelper.aggregateVotes(post, asyncVoteResult);
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
        final Map<Key<? extends Post>, List<LoadResult<Key<Vote>>>> votedKeysMap =
                new LinkedHashMap<>(limit);
        final Map<Key<? extends Post>, LoadResult<Key<Comment>>> commentKeysMap =
                new LinkedHashMap<>(limit);

        final List<ForumPost> posts = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            // Get post key.
            final ForumPost post = queryIterator.next();
            final Key<? extends Post> postKey = post.getKey();
            posts.add(post);

            votedKeysMap.put(postKey, VoteHelper.loadAsyncVoteKeys(userKey, postKey));
            commentKeysMap.put(postKey, CommentHelper.loadAsyncComments(userKey, postKey));
        }

        VoteHelper.aggregateVotes(posts, votedKeysMap);
        CommentHelper.aggregateComments(posts, commentKeysMap);

        return CollectionResponse.<ForumPost>builder()
                .setItems(posts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
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
