package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.yoloo.android.backend.counter.user.UserFollowerCounter;
import com.yoloo.android.backend.counter.user.UserFoloweeCounter;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserIndexShard;
import com.yoloo.android.backend.model.user.UserIndexShardCounter;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class UserController {

    /**
     * Default number of shards.
     */
    private static final int INITIAL_SHARDS = 3;

    private static final Logger logger =
            Logger.getLogger(UserController.class.getSimpleName());

    /**
     * A random number generating, for distributing writes across shards.
     */
    private final Random generator = new Random();

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

    public Account get(final String websafeUserKey) {
        Key<Account> userKey = Key.create(websafeUserKey);

        Account account = ofy().load().key(userKey).now();

        List<UserIndexShard> shards =
                ofy().load().type(UserIndexShard.class).ancestor(userKey).list();

        setExtraUserFields(account, shards);

        return account;
    }

    public Account add(final GoogleIdToken.Payload payload) {
        // Allocate a new Id.
        Key<Account> userKey = ofy().factory().allocateId(Account.class);

        Account account = Account.builder(userKey)
                .setUsername((String) payload.get("name"))
                .setEmail(payload.getEmail())
                .setProvider(Account.Provider.GOOGLE)
                .setRealname((String) payload.get("given_name"))
                .setProfileImageUrl(payload.get("picture") != null ?
                        (String) payload.get("picture") : "https://s31.postimg.org/bgayiukkb/dummy_user_icon.jpg")
                .build();

        final UserIndexShard userIndexShard =
                UserIndexShard.builder(1, userKey).build();

        return save(account, userIndexShard);
    }

    public Account add(final String[] values) {
        // Allocate a new Id.
        final Key<Account> userKey = ofy().factory().allocateId(Account.class);

        final Account account = Account.builder(userKey)
                .setUsername(values[0])
                .setPassword(values[1])
                .setEmail(values[2])
                .setProvider(Account.Provider.YOLOO)
                .setProfileImageUrl("https://s31.postimg.org/bgayiukkb/dummy_user_icon.jpg")
                .build();

        final UserIndexShard userIndexShard =
                UserIndexShard.builder(1, userKey).build();

        return save(account, userIndexShard);
    }

    public void follow(final String websafeFolloweeKey, final User user) {
        final Key<Account> followerKey = Key.create(user.getUserId());
        final Key<Account> followeeKey = Key.create(websafeFolloweeKey);

        final Follow follow = Follow.newInstance(followerKey, followeeKey);

        // Find how many shards are in this counter.
        int numShards = getShardCount(followeeKey);

        // Choose the shard randomly from the available shards.
        final int shardNum = generator.nextInt(numShards) + 1;

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                UserIndexShard followerShard;
                UserIndexShard followeeShard;
                long followeeCount;
                long followerCount;

                try {
                    followerShard = ofy().load().type(UserIndexShard.class)
                            .parent(followerKey).id(shardNum).safe();

                    new UserFoloweeCounter(followerShard).increase();

                    //followeeCount = followerShard.getFolloweeCount() + 1;

                    followeeShard = ofy().load().type(UserIndexShard.class)
                            .parent(followeeKey).id(shardNum).safe();

                    new UserFollowerCounter(followeeShard).increase();

                    //followerCount = followeeShard.getFollowerCount() + 1;
                } catch (NotFoundException e) {
                    followerShard = UserIndexShard.builder(shardNum, followerKey).build();

                    new UserFoloweeCounter(followerShard).increase();
                    //followeeCount = 1;

                    followeeShard = UserIndexShard.builder(shardNum, followeeKey).build();

                    new UserFollowerCounter(followeeShard).increase();
                    //followerCount = 1;
                }
                //followerShard.setFolloweeCount(followeeCount);
                //followeeShard.setFollowerCount(followerCount);

                ofy().save().entities(follow, followerShard, followeeShard);
            }
        });
    }

    public void unfollow(final String websafeFolloweeKey, final User user) {
        Key<Account> followerKey = Key.create(user.getUserId());
        Key<Account> followeeKey = Key.create(websafeFolloweeKey);

        final Key<Follow> followKey = ofy().load().type(Follow.class)
                .ancestor(followerKey).filter("followeeKey =", followeeKey)
                .keys().first().now();

        final UserIndexShard followerShard =
                ofy().load().type(UserIndexShard.class)
                        .ancestor(followerKey).first().now();

        new UserFoloweeCounter(followerShard).decrease();
        //followerShard.setFolloweeCount(followerShard.getFolloweeCount() - 1);

        final UserIndexShard followeeShard =
                ofy().load().type(UserIndexShard.class)
                        .ancestor(followeeKey).first().now();

        new UserFollowerCounter(followeeShard).decrease();
        //followeeShard.setFollowerCount(followeeShard.getFollowerCount() - 1);

        ofy().delete().keys(followKey);
        ofy().save().entities(followerShard, followeeShard).now();
    }

    private Account save(final Account account,
                         final UserIndexShard userIndexShard) {
        return ofy().transact(new Work<Account>() {
            @Override
            public Account run() {
                ofy().save().entities(account, userIndexShard).now();

                setExtraUserFields(account, Collections.singletonList(userIndexShard));
                return account;
            }
        });
    }

    private void setExtraUserFields(Account account, List<UserIndexShard> shards) {
        long followeeSum = 0L;
        long followerSum = 0L;
        long questionSum = 0L;

        for (UserIndexShard shard : shards) {
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