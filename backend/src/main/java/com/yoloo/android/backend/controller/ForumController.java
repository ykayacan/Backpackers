package com.yoloo.android.backend.controller;

import com.google.api.client.util.Strings;
import com.google.appengine.api.users.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.PostFactory;
import com.yoloo.android.backend.counter.user.UserQuestionCounter;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.model.question.QuestionCounter;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserIndexShard;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class ForumController {

    private static final Logger logger =
            Logger.getLogger(ForumController.class.getName());

    public static ForumController newInstance() {
        return new ForumController();
    }

    public Question get(final String websafeQuestionKey, final User user) {
        // The key of the question
        Key<Question> questionKey = Key.create(websafeQuestionKey);
        // The key of the Question counter related to Question
        Key<QuestionCounter> questionCounterKey = getQuestionCounterKey(questionKey);
        // The key of the Location counter related to Question
        Key<Location> locationKey = getLocationKey(questionKey);

        LoadResult<Key<Like>> likeLoadResult = getQuestionLikeKeyAsync(user, questionKey);

        // batch async load by keys.
        Map<Key<Object>, Object> map =
                ofy().load().keys(questionKey, questionCounterKey, locationKey);

        @SuppressWarnings("SuspiciousMethodCalls")
        Question question = (Question) map.get(questionKey);
        @SuppressWarnings("SuspiciousMethodCalls")
        QuestionCounter questionCounter = (QuestionCounter) map.get(questionCounterKey);
        @SuppressWarnings("SuspiciousMethodCalls")
        Location location = (Location) map.get(locationKey);

        setExtraQuestionFields(likeLoadResult, question, questionCounter, location);

        return question;
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
                         final String awardedBy,
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

        Key<Account> awardedByKey = null;
        if (!Strings.isNullOrEmpty(awardedBy)) {
            awardedByKey = Key.create(awardedBy);
        }

        // Create a new post.
        final ForumPost post = new PostFactory()
                .createForumPost(postKey, account, content, hashtags,
                        locations, isLocked, awardedByKey, awardRep);

        // Increase question counter of the current user.
        final UserIndexShard counter = loadAndIncreaseUserQuestionCounter(userKey);

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
                            final HttpServletRequest request,
                            final User user) {
        Key<ForumPost> postKey = Key.create(websafePostId);

        ForumPost post = ofy().load().key(postKey).now();

        Key<Question> questionKey = Key.create(websafePostId);
        Key<QuestionCounter> questionCounterKey = getQuestionCounterKey(questionKey);
        Key<Location> locationKey = getLocationKey(questionKey);

        // batch async load by keys.
        Map<Key<Object>, Object> map =
                ofy().load().keys(questionKey, questionCounterKey, locationKey);

        // Check if any part of the entity is changed.
        boolean isUpdated = false;

        @SuppressWarnings("SuspiciousMethodCalls")
        Question question = (Question) map.get(questionKey);
        @SuppressWarnings("SuspiciousMethodCalls")
        QuestionCounter questionCounter = (QuestionCounter) map.get(questionCounterKey);
        @SuppressWarnings("SuspiciousMethodCalls")
        Location location = (Location) map.get(locationKey);

        if (!Strings.isNullOrEmpty(content)) {
            post.setContent(content);
            isUpdated = true;
        }
        if (!Strings.isNullOrEmpty(hashtags)) {
            post.getHashtags().clear();
            post.getHashtags().addAll(StringUtil.splitValueByToken(hashtags, ","));
            isUpdated = true;
        }
        if (!Strings.isNullOrEmpty(locations)) {
            // Find related Location entities with the given post key.
            List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                    .filter("postKey =", postKey).keys().list();

            // Delete all entities.
            ofy().delete().keys(locationKeys);

            // Generate locations from given string with immutable way.
            Set<Location> locationSet =
                    ImmutableSet.copyOf(LocationHelper.getLocations(locations, postKey));
            post.setLocations(locationSet);

            ofy().save().entities(locationSet);

            isUpdated = true;
        }

        if (isUpdated) {
            post.setUpdatedAt(new Date());
        }

        question.setUpdatedAt(new Date());

        ofy().save().entities(question, location);

        setExtraQuestionFields(null, question, questionCounter, location);

        return post;
    }

    private void setExtraQuestionFields(final LoadResult<Key<Like>> result,
                                        final Question question,
                                        final QuestionCounter questionCounter,
                                        final Location location) {
        Account user = question.getParentUser();

        question.setUsername(user.getUsername());
        question.setProfileImageUrl(user.getProfileImageUrl());
        question.setLocation(location);
        question.setLikesCount(questionCounter.getLikesCount());
        question.setCommentsCount(questionCounter.getCommentsCount());
        question.setLiked(isUserLiked(result));
    }

    private Location createLocation(final Key<Question> questionKey,
                                    final String locationName,
                                    final String latLng) {
        return new Location.Builder()
                //.setPostKey(questionKey)
                .setName(locationName)
                .setGeoPt(LocationHelper.createLocationFromString(latLng))
                .build();
    }

    private Question createQuestion(final String title,
                                    final String message,
                                    final String hashtag,
                                    final Key<Question> questionKey) {
        return Question.builder(questionKey, questionKey.<Account>getParent())
                .setTitle(title) // TODO: 8.07.2016 May be capitalize?
                .setContent(message)
                .setHashtag(StringUtil.splitValueByComma(hashtag))
                .build();
    }

    private boolean isUserLiked(final LoadResult<Key<Like>> result) {
        return result != null && result.now() != null;
    }

    private LoadResult<Key<Like>> getQuestionLikeKeyAsync(User user, Key<Question> key) {
        return ofy().load().type(Like.class)
                .ancestor(Key.create(user.getUserId()))
                .filter("likeableEntity", key)
                .keys().first();
    }

    private Key<Location> getLocationKey(Key<Question> questionKey) {
        return ofy().load().type(Location.class)
                .filter("question =", questionKey).keys().first().now();
    }

    private Key<QuestionCounter> getQuestionCounterKey(Key<Question> questionKey) {
        return ofy().load().type(QuestionCounter.class)
                .ancestor(questionKey.getRoot()).keys().first().now();
    }

    private UserIndexShard loadAndIncreaseUserQuestionCounter(Key<Account> userKey) {
        // Find first shard and increase the question counter.
        // Users don't post a question rapidly so it is safe to use first shard.
        UserIndexShard counter = ofy().load().type(UserIndexShard.class)
                .ancestor(userKey).first().now();

        new UserQuestionCounter(counter).increase();

        return counter;
    }
}
