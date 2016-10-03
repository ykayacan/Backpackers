package com.backpackers.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NotificationController {

    private static final Logger logger =
            Logger.getLogger(NotificationController.class.getName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    public static NotificationController newInstance() {
        return new NotificationController();
    }

    public void add(final String websafeReceiverId, final Notification.Action action,
                    final String content, final String websafePostId,
                    final String websafeCommentId, final String photoUrl,
                    final User user) {
        Key<Account> senderKey = Key.create(user.getUserId());
        Key<Account> receiverKey = Key.create(websafeReceiverId);

        final Notification notification = getMessage(action, senderKey, receiverKey, content,
                websafePostId, websafeCommentId);

        OfyHelper.ofy().save().entity(notification);

        sendPushNotification(receiverKey, action);
    }

    private void sendPushNotification(Key<Account> receiverKey, Notification.Action action) {
        final RegistrationRecord record = OfyHelper.ofy().load().type(RegistrationRecord.class)
                .ancestor(receiverKey).first().now();
    }

    public void remove(final String websafeNotificationId, final User user) {
        final Key<Notification> notificationKey = Key.create(websafeNotificationId);

        OfyHelper.ofy().delete().key(notificationKey);
    }

    public CollectionResponse<Notification> list(final String cursor, Integer limit,
                                                 final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Key<Account> userKey = Key.create(user.getUserId());

        Query<Notification> query = OfyHelper.ofy().load().type(Notification.class).ancestor(userKey);

        query = query.order("-createdAt");

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<Notification> queryIterator = query.iterator();

        final List<Notification> notifications = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            notifications.add(queryIterator.next());
        }

        return CollectionResponse.<Notification>builder()
                .setItems(notifications)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    private Notification getMessage(Notification.Action action, Key<Account> senderKey,
                                    Key<Account> receiverKey, String content,
                                    String websafePostId, String websafeCommentId) {
        final Account sender = getAccount(senderKey);

        switch (action) {
            case FOLLOW:
                return Notification.builder(senderKey, receiverKey)
                        .setAction(Notification.Action.FOLLOW)
                        .setUsername(sender.getUsername())
                        .setProfileImageUrl(sender.getProfileImageUrlLink())
                        .setLocale(sender.getLocale())
                        .build();
            case MENTION:
                return Notification.builder(senderKey, receiverKey)
                        .setAction(Notification.Action.MENTION)
                        .setUsername(sender.getUsername())
                        .setProfileImageUrl(sender.getProfileImageUrlLink())
                        .setCommentId(websafeCommentId)
                        .setContent(content)
                        .setLocale(sender.getLocale())
                        .build();
            case COMMENT:
                return Notification.builder(senderKey, receiverKey)
                        .setAction(Notification.Action.COMMENT)
                        .setUsername(sender.getUsername())
                        .setProfileImageUrl(sender.getProfileImageUrlLink())
                        .setPostId(websafePostId)
                        .setContent(content)
                        .setLocale(sender.getLocale())
                        .build();
            case ASK:
                return Notification.builder(senderKey, receiverKey)
                        .setAction(Notification.Action.ASK)
                        .setUsername(sender.getUsername())
                        .setProfileImageUrl(sender.getProfileImageUrlLink())
                        .setPostId(websafePostId)
                        .setLocale(sender.getLocale())
                        .build();
            default:
                return null;
        }
    }

    private Account getAccount(Key<Account> userKey) {
        return OfyHelper.ofy().load().key(userKey).now();
    }
}
