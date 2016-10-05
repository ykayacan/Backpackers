package com.backpackers.android.backend.controller;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.backpackers.android.backend.badge.Badge;
import com.backpackers.android.backend.badge.FirstUserBadge;
import com.backpackers.android.backend.badge.NewbieBadge;
import com.backpackers.android.backend.badge.WelcomeBadge;
import com.backpackers.android.backend.factory.user.GoogleUserFactory;
import com.backpackers.android.backend.factory.user.YolooUserFactory;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.follow.Follow;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.util.NotificationHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.factory.user.UserFactory;
import com.backpackers.android.backend.model.user.UserCounterShard;
import com.backpackers.android.backend.model.user.UserIndexShardCounter;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class UserController {

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

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
     * <p>
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
    public Account get(final String websafeUserId, final User user) {
        final Key<Account> requesterKey = Key.create(user.getUserId());
        final Key<Account> userKey = Key.create(websafeUserId);

        final Account account = ofy().load().key(userKey).now();

        final List<UserCounterShard> shards =
                ofy().load().type(UserCounterShard.class).ancestor(userKey).list();

        final Key<?> key = ofy().load().type(Follow.class)
                .ancestor(requesterKey)
                .filter("followeeKey =", userKey)
                .keys().first().now();

        account.setFollowing(key != null);

        setExtraUserFields(account, shards);

        return account;
    }

    /**
     * Get account.
     *
     * @param websafeUserId the websafe user key
     * @return the account
     */
    public Account getSelf(final String websafeUserId) {
        final Key<Account> userKey = Key.create(websafeUserId);

        final Account account = ofy().load().key(userKey).now();

        final List<UserCounterShard> shards =
                ofy().load().type(UserCounterShard.class).ancestor(userKey).list();

        setExtraUserFields(account, shards);

        if (Months.monthsBetween(new DateTime(account.getCreatedAt()),
                new DateTime()).getMonths() == 1) {
            Badge badge = new FirstUserBadge();
            account.addBadge(new FirstUserBadge());

            ofy().save().entity(account);

            RegistrationRecord record = ofy().load().type(RegistrationRecord.class)
                    .ancestor(userKey).first().now();

            NotificationHelper.sendAchievementNotification(record.getRegId(),
                    badge.getName(), badge.getImageUrl(), badge.getContent());
        }

        return account;
    }

    /**
     * Get followers.
     *
     * @param websafeUserId the websafe user key
     * @return the account
     */
    public CollectionResponse<Account> getFollowers(final String websafeUserId,
                                                    final String cursor,
                                                    Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(websafeUserId);

        Query<Follow> query = ofy().load().type(Follow.class);

        query = query.filter("followeeKey =", userKey);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<Follow> queryIterator = query.iterator();

        final List<Key<Account>> followerKeys = new ArrayList<>(limit);

        while (queryIterator.hasNext()) {
            // Get post key.
            final Follow follow = queryIterator.next();
            followerKeys.add(follow.getParentUserKey());
        }

        final Collection<Account> accounts = ofy().load().keys(followerKeys).values();

        return CollectionResponse.<Account>builder()
                .setItems(accounts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    /**
     * Get followers.
     *
     * @param websafeUserId the websafe user key
     * @return the account
     */
    public CollectionResponse<Account> getFollowings(final String websafeUserId,
                                                     final String cursor,
                                                     Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(websafeUserId);

        Query<Follow> query = ofy().load().type(Follow.class).ancestor(userKey);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<Follow> queryIterator = query.iterator();

        final List<Key<Account>> followingKeys = new ArrayList<>(limit);

        while (queryIterator.hasNext()) {
            // Get post key.
            final Follow follow = queryIterator.next();
            followingKeys.add(follow.getFolloweeKey());
        }

        final Collection<Account> accounts = ofy().load().keys(followingKeys).values();

        return CollectionResponse.<Account>builder()
                .setItems(accounts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    /**
     * Add account.
     *
     * @param payload the payload
     * @return the account
     */
    public Account add(final GoogleIdToken.Payload payload, final String locale)
            throws ConflictException {
        final String email = payload.getEmail();

        Key<Account> userKey = ofy().load().type(Account.class)
                .filter("email =", email)
                .keys().first().now();

        final Account account;

        // User already used Google login before, so return existing user.
        if (userKey != null) {
            account = ofy().load().key(userKey).now();
            if (account.getProvider().compareTo(Account.Provider.GOOGLE) == 0) {
                return account;
            } else {
                throw new ConflictException("Email already exists.");
            }
        } else {
            // Allocate a new Id.
            final Key<Account> newUserKey = ofy().factory().allocateId(Account.class);

            account = UserFactory.getAccount(new GoogleUserFactory(newUserKey, payload, locale));

            final UserCounterShard shard = UserCounterShard.builder(1, newUserKey).build();

            account.addBadge(new WelcomeBadge());
            account.addBadge(new NewbieBadge());
            account.addBadge(new FirstUserBadge());

            return save(account, shard);
        }
    }

    /**
     * Add account.
     *
     * @param values the values
     * @return the account
     */
    public Account add(final String[] values, final String locale) {
        // Allocate a new Id.
        final Key<Account> userKey = ofy().factory().allocateId(Account.class);

        final Account account = UserFactory.getAccount(new YolooUserFactory(userKey, values, locale));

        final UserCounterShard shard = UserCounterShard.builder(1, userKey).build();

        account.addBadge(new WelcomeBadge());
        account.addBadge(new NewbieBadge());
        account.addBadge(new FirstUserBadge());

        return save(account, shard);
    }

    public Account update(final String mediaId, String badgeName, final User user) {
        Account a = null;
        Media m = null;

        if (!Strings.isNullOrEmpty(mediaId)) {
            Map<Key<Object>, Object> map = ofy().load()
                    .keys(Key.<Account>create(user.getUserId()), Key.<Media>create(mediaId));

            for (Object o : map.values()) {
                if (o instanceof Account) {
                    a = (Account) o;
                } else if (o instanceof Media) {
                    m = (Media) o;
                }
            }

            a.setProfileImageUrl(m.getUrl().getValue().concat("=s120"));

            List<Object> userEntities = ofy().load().group(ForumPost.class, Comment.class)
                    .ancestor(a.getKey()).list();

            List<Object> updateList = new ArrayList<>(userEntities.size());

            for (Object o : userEntities) {
                if (o instanceof ForumPost) {
                    ((ForumPost) o).setProfileImageUrl(m.getUrl());
                } else if (o instanceof Comment) {
                    ((Comment) o).setProfileImageUrl(m.getUrl());
                }
                updateList.add(o);
            }

            ofy().save().entities(updateList);
        }

        return a;
    }

    public void remove(final User user) {

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

        final RegistrationRecord record = ofy().load().type(RegistrationRecord.class)
                .ancestor(followeeKey).first().now();

        final Account sender = ofy().load().key(followerKey).now();

        final Notification notification = getNotification(sender, followeeKey);

        runFollowTransact(followerKey, followeeKey, follow, shardNum, notification);

        NotificationHelper.sendFollowNotification(record.getRegId(),
                sender.getUsername(), sender.getWebsafeKey());
    }

    /**
     * Unfollow.
     *
     * @param websafeFolloweeId the websafe followee id
     * @param user              the user
     */
    public void unFollow(final String websafeFolloweeId, final User user) {
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

        runUnFollowTransact(followKey, followerShard, followeeShard);
    }

    private void runFollowTransact(final Key<Account> followerKey,
                                   final Key<Account> followeeKey,
                                   final Follow follow, final int shardNum,
                                   final Notification notification) {
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

                ofy().save().entities(follow, followerShard, followeeShard, notification);
            }
        });
    }

    private void runUnFollowTransact(final Key<Follow> followKey,
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

    private Account save(final Account account, final UserCounterShard shard) {
        return ofy().transact(new Work<Account>() {
            @Override
            public Account run() {
                Map<Key<Object>, Object> map = ofy().save().entities(account, shard).now();

                for (Key<Object> key : map.keySet()) {
                    if (key.getKind().equals("Account")) {
                        setExtraUserFields(account, Collections.singletonList(shard));
                        return (Account) ofy().load().key(key).now();
                    }
                }

                return null;
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

    private Notification getNotification(Account sender, Key<Account> followeeKey) {
        return Notification.builder(sender.getKey(), followeeKey)
                .setAction(Notification.Action.FOLLOW)
                .setUsername(sender.getUsername())
                .setProfileImageUrl(sender.getProfileImageUrlLink())
                .setLocale(sender.getLocale())
                .build();
    }
}