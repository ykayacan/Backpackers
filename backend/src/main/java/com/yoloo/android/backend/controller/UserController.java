package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.yoloo.android.backend.factory.user.GoogleUserFactory;
import com.yoloo.android.backend.factory.user.UserFactory;
import com.yoloo.android.backend.factory.user.YolooUserFactory;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserCounterShard;
import com.yoloo.android.backend.model.user.UserIndexShardCounter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class UserController {

    /**
     * Default number of shards.
     */
    private static final int INITIAL_SHARDS = 3;

    private static final Logger logger =
            Logger.getLogger(UserController.class.getName());

    /**
     * New instance user controller.
     *
     * @return the user controller
     */
    public static UserController newInstance() {
        return new UserController();
    }

    /**
     * Add shards for parentUserKey.
     *
     * First, it looks for {@link UserIndexShardCounter}.
     * If it exists, then increse value by given value. If not creates a new instance.
     *
     * @param count         the count
     * @param parentUserKey the parent user key
     */
    public static void addShards(final int count, final Key<Account> parentUserKey) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                UserIndexShardCounter counter;
                int value;
                try {
                    counter = ofy().load().type(UserIndexShardCounter.class)
                            .ancestor(parentUserKey).first().safe();
                    value = counter.getShardCount() + count;
                } catch (NotFoundException e) {
                    counter = UserIndexShardCounter.newInstance(parentUserKey);
                    value = INITIAL_SHARDS + count;
                }
                counter.setShardCount(value);
                ofy().save().entity(counter).now();
            }
        });
    }

    /**
     * Get account.
     *
     * @param websafeUserId the websafe user key
     * @return the account
     */
    public Account get(final String websafeUserId) {
        final Key<Account> userKey = Key.create(websafeUserId);

        final Account account = ofy().load().key(userKey).now();

        final List<UserCounterShard> shards =
                ofy().load().type(UserCounterShard.class).ancestor(userKey).list();

        setExtraUserFields(account, shards);

        return account;
    }

    /**
     * Add account.
     *
     * @param payload the payload
     * @return the account
     */
    public Account add(final GoogleIdToken.Payload payload) {
        // Allocate a new Id.
        final Key<Account> userKey = ofy().factory().allocateId(Account.class);

        final Account account = UserFactory.getAccount(new GoogleUserFactory(userKey, payload));

        final UserCounterShard shard = UserCounterShard.builder(1, userKey).build();

        return save(account, shard);
    }

    /**
     * Add account.
     *
     * @param values the values
     * @return the account
     */
    public Account add(final String[] values) {
        // Allocate a new Id.
        final Key<Account> userKey = ofy().factory().allocateId(Account.class);

        final Account account = UserFactory.getAccount(new YolooUserFactory(userKey, values));

        final UserCounterShard shard = UserCounterShard.builder(1, userKey).build();

        return save(account, shard);
    }

    /**
     * Follow.
     *
     * @param websafeFolloweeId the websafe followee id
     * @param user              the user
     */
    public void follow(final String websafeFolloweeId, final User user) {
        final Key<Account> followerKey = Key.create(user.getUserId());
        final Key<Account> followeeKey = Key.create(websafeFolloweeId);

        final Follow follow = Follow.newInstance(followerKey, followeeKey);

        // Find how many shards are in this counter.
        int numShards = getShardCount(followeeKey);

        // Choose the shard randomly from the available shards.
        // Add +1 to numShards to make it inclusive.
        final int shardNum = ThreadLocalRandom.current().nextInt(1, numShards + 1);

        runFollowTransact(followerKey, followeeKey, follow, shardNum);
    }

    /**
     * Unfollow.
     *
     * @param websafeFolloweeId the websafe followee id
     * @param user              the user
     */
    public void unfollow(final String websafeFolloweeId, final User user) {
        Key<Account> followerKey = Key.create(user.getUserId());
        Key<Account> followeeKey = Key.create(websafeFolloweeId);

        final Key<Follow> followKey = ofy().load().type(Follow.class)
                .ancestor(followerKey).filter("followeeKey =", followeeKey)
                .keys().first().now();

        final UserCounterShard followerShard = ofy().load().type(UserCounterShard.class)
                .ancestor(followerKey).first().now();

        followerShard.setFolloweeCount(followerShard.getFolloweeCount() - 1);

        final UserCounterShard followeeShard = ofy().load().type(UserCounterShard.class)
                .ancestor(followeeKey).first().now();

        followeeShard.setFollowerCount(followeeShard.getFollowerCount() - 1);

        runUnfollowTransact(followKey, followerShard, followeeShard);
    }

    private void runFollowTransact(final Key<Account> followerKey,
                                   final Key<Account> followeeKey,
                                   final Follow follow, final int shardNum) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                UserCounterShard followerShard;
                UserCounterShard followeeShard;
                long followeeCount;
                long followerCount;

                try {
                    followerShard = ofy().load().type(UserCounterShard.class)
                            .parent(followerKey).id(shardNum).safe();
                    followeeCount = followerShard.getFolloweeCount() + 1;

                    followeeShard = ofy().load().type(UserCounterShard.class)
                            .parent(followeeKey).id(shardNum).safe();
                    followerCount = followeeShard.getFollowerCount() + 1;
                } catch (NotFoundException e) {
                    followerShard = UserCounterShard.builder(shardNum, followerKey).build();
                    followeeCount = 1;

                    followeeShard = UserCounterShard.builder(shardNum, followeeKey).build();
                    followerCount = 1;
                }
                followerShard.setFolloweeCount(followeeCount);
                followeeShard.setFollowerCount(followerCount);

                ofy().save().entities(follow, followerShard, followeeShard);
            }
        });
    }

    private void runUnfollowTransact(final Key<Follow> followKey,
                                     final UserCounterShard followerShard,
                                     final UserCounterShard followeeShard) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().delete().keys(followKey);
                ofy().save().entities(followerShard, followeeShard).now();
            }
        });
    }

    private Account save(final Account account,
                         final UserCounterShard shard) {
        return ofy().transact(new Work<Account>() {
            @Override
            public Account run() {
                ofy().save().entities(account, shard).now();

                setExtraUserFields(account, Collections.singletonList(shard));
                return account;
            }
        });
    }

    private void setExtraUserFields(Account account, List<UserCounterShard> shards) {
        long followeeSum = 0L;
        long followerSum = 0L;
        long questionSum = 0L;

        for (UserCounterShard shard : shards) {
            followeeSum += shard.getFolloweeCount();
            followerSum += shard.getFollowerCount();
            questionSum += shard.getQuestionCount();
        }

        account.setFollowerCount(followerSum);
        account.setFolloweeCount(followeeSum);
        account.setQuestionCount(questionSum);
    }

    private int getShardCount(final Key<Account> parentUserKey) {
        try {
            return ofy().load().type(UserIndexShardCounter.class)
                    .ancestor(parentUserKey).first().safe().getShardCount();
        } catch (com.googlecode.objectify.NotFoundException e) {
            return INITIAL_SHARDS;
        }
    }
}