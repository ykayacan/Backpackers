package com.backpackers.android.backend.controller;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.common.collect.ImmutableList;

import com.backpackers.android.backend.GcsSingleton;
import com.backpackers.android.backend.badge.Badge;
import com.backpackers.android.backend.badge.EntrepreneurBadge;
import com.backpackers.android.backend.factory.post.ForumPostFactory;
import com.backpackers.android.backend.factory.post.PostFactory;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.hashtag.HashTag;
import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.media.Metadata;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.model.user.UserCounterShard;
import com.backpackers.android.backend.model.vote.Vote;
import com.backpackers.android.backend.util.CommentHelper;
import com.backpackers.android.backend.util.NotificationHelper;
import com.backpackers.android.backend.util.StringUtil;
import com.backpackers.android.backend.util.VoteHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

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
        final Key<ForumPost> postKey = Key.create(websafePostKey);
        final Key<Account> userKey = Key.create(user.getUserId());

        final ForumPost post = ofy().load().key(postKey).now();

        final List<LoadResult<Key<Vote>>> votedKeys = VoteHelper.loadAsyncVoteKeys(userKey, postKey);
        final LoadResult<Key<Comment>> commentKeysMap = CommentHelper.loadAsyncComments(userKey, postKey);

        VoteHelper.aggregateVotes(post, votedKeys);
        CommentHelper.aggregateComments(post, commentKeysMap);

        return post;
    }

    /**
     * Creates a new Question with given parameters.
     *
     * @return the Question
     */
    public ForumPost add(final String content, final String hashTags, final String location,
                         final String mediaIds, final Boolean isLocked, final Integer awardRep,
                         final HttpServletRequest request, final User user) {
        // Create parent user key.
        final Key<Account> userKey = Key.create(user.getUserId());
        /*final LoadResult<Key<Follow>> followResult = ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().first();*/

        // Get related account.
        final Account account = ofy().load().key(userKey).now();

        try {
            ofy().load().type(ForumPost.class).ancestor(userKey).keys().first().safe();
        } catch (NotFoundException e) {
            final Badge badge = new EntrepreneurBadge();
            if (!account.getBadges().contains(badge)) {
                account.addBadge(new EntrepreneurBadge());

                RegistrationRecord record = ofy().load().type(RegistrationRecord.class)
                        .ancestor(userKey).first().now();

                if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
                    NotificationHelper.sendAchievementNotification(record.getRegId(),
                            badge.getName(), badge.getImageUrl(), badge.getContent());
                }
            }
        }

        // Allocate an id with parent user key.
        final Key<ForumPost> postKey =
                ofy().factory().allocateId(userKey, ForumPost.class);

        Map<Key<Media>, Media> mediaMap = getKeyMediaMap(mediaIds, postKey);

        final List<String> bareHashTags = StringUtil.split(hashTags, ",");
        final List<HashTag> hashTagList = getHashTags(bareHashTags);

        // Create a new post.
        final ForumPost post = (ForumPost) PostFactory.create(
                new ForumPostFactory(postKey, account, content, bareHashTags,
                        location, mediaMap, isLocked, awardRep));

        // TODO: 12.08.2016 Gamification here.

        // Increase question counter of the current user.
        final UserCounterShard shard = loadAndIncreaseUserQuestionCounter(userKey);

        // Batch save entities
        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        builder.add(post)
                .add(account)
                .add(shard)
                .addAll(hashTagList);

        if (!Strings.isNullOrEmpty(location)) {
            builder.add(post.getLocation());
        }

        if (mediaMap != null) {
            builder.addAll(mediaMap.values());
        }

        ofy().save().entities(builder.build());

        //TimelineUtil.updateTimeline(userKey, followResult, post);

        return post;
    }

    public ForumPost update(final String websafePostId,
                            final String content,
                            final String hashtags,
                            final String location,
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

        updateMedia(post, mediaIds);
        updateContent(post, content);
        List<HashTag> hashTags = updateHashTags(post, hashtags);
        updateLocation(post, location);
        updateLocked(post, isLocked);
        updateAward(post, userKey, awardRep);
        updateAccepted(post, isAccepted);
        updateDate(post);

        VoteHelper.aggregateVotes(post, asyncVoteResult);
        CommentHelper.aggregateComments(post, asyncCommentsResult);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        builder.add(post)
                .add(post.getLocation());

        if (hashTags != null) {
            builder.addAll(hashTags);
        }

        ofy().save().entities(builder.build());

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
        final Key<ForumPost> postKey = Key.create(websafePostId);
        final Key<Account> userKey = Key.create(user.getUserId());

        // Get all location keys belong to post.
        final List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().list();

        // Get all vote keys belong to post.
        final List<Key<Vote>> voteKeys = ofy().load().type(Vote.class)
                .filter("postKey =", postKey)
                .keys().list();

        // Get all medias belong to post.
        final List<Media> medias = ofy().load().type(Media.class)
                .filter("websafePostId =", websafePostId)
                .list();

        // Get user counter shard.
        final UserCounterShard shard = ofy().load().type(UserCounterShard.class)
                .ancestor(userKey).first().now();

        // Reduce count by one.
        shard.setQuestionCount(shard.getQuestionCount() - 1);

        final List<Object> deleteList = ImmutableList.builder()
                .addAll(locationKeys)
                .addAll(voteKeys)
                .add(postKey)
                .build();

        ofy().delete().entities(deleteList);
        ofy().save().entity(shard);

        // Delete medias in storage.
        if (!medias.isEmpty()) {
            ThreadManager.createThreadForCurrentRequest(new Runnable() {
                @Override
                public void run() {
                    for (Media media : medias) {
                        final Metadata metadata = media.getMeta();
                        final GcsFilename gcsFilename =
                                new GcsFilename(metadata.getBucketName(), metadata.getObjectName());
                        try {
                            GcsSingleton.getGcsService().delete(gcsFilename);
                        } catch (IOException e) {
                            logger.info("IOException: " + e.getMessage());
                        }
                    }
                }
            }).run();

            ofy().delete().entities(medias);
        }
    }

    public CollectionResponse<ForumPost> list(final String sort,
                                              final String cursor,
                                              Integer limit,
                                              String targetUserId,
                                              final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(user.getUserId());

        Query<ForumPost> query = ofy().load().type(ForumPost.class);

        if (targetUserId != null) {
            query = query.ancestor(Key.<Account>create(targetUserId));
        }

        if (sort == null || sort.equals(SORT_BY_NEWEST)) {
            query = query.order("-createdAt");
        } else if (sort.equals(SORT_BY_HOT)) {
            query = query.order("-rank");
        }

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<ForumPost> queryIterator = query.iterator();

        // Store async batch in a hashmap. LinkedHashMap preserve insertion order.
        final Map<Key<? extends AbstractPost>, List<LoadResult<Key<Vote>>>> votedKeysMap =
                new LinkedHashMap<>(limit);
        final Map<Key<? extends AbstractPost>, LoadResult<Key<Comment>>> commentKeysMap =
                new LinkedHashMap<>(limit);

        final List<ForumPost> posts = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            // Get post key.
            final ForumPost post = queryIterator.next();
            final Key<? extends AbstractPost> postKey = post.getKey();
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

    private List<HashTag> getHashTags(List<String> bareHashTags) {
        final List<HashTag> hashTagList = new ArrayList<>(bareHashTags.size());

        for (String bareHashTag : bareHashTags) {
            hashTagList.add(new HashTag(bareHashTag.toLowerCase()));
        }
        return hashTagList;
    }

    private Map<Key<Media>, Media> getKeyMediaMap(String mediaIds, Key<ForumPost> postKey) {
        Map<Key<Media>, Media> mediaMap = null;
        if (!Strings.isNullOrEmpty(mediaIds)) {
            final List<Key<Media>> mediaKeys = new ArrayList<>(3);
            final List<String> mediaIdList = StringUtil.splitValueByToken(mediaIds, ",");

            for (String mediaId : mediaIdList) {
                mediaKeys.add(Key.<Media>create(mediaId));
            }
            mediaMap = ofy().load().keys(mediaKeys);

            final String websafePostKey = postKey.toWebSafeString();
            for (Media media : mediaMap.values()) {
                media.setWebsafePostId(websafePostKey);
            }
        }
        return mediaMap;
    }
}
