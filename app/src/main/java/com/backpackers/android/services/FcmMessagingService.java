package com.backpackers.android.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.backpackers.android.ui.comment.CommentActivity;
import com.backpackers.android.R;
import com.backpackers.android.ui.profile.ProfileActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import timber.log.Timber;

public class FcmMessagingService extends FirebaseMessagingService {

    public static final int MESSAGE_NOTIFICATION_ID = 435345;

    private NotificationCompat.Builder mBuilder;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        Timber.d("onMessageReceived()");

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Timber.d("Message data payload: %s", remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }

        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage message) {
        final String action = message.getData().get("action");

        Timber.d("Action: %s", action);

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = null;

        switch (action) {
            case "FOLLOW":
                builder = getFollowNotification(message);
                break;
            case "COMMENT":
                builder = getCommentNotification(message);
                break;
            case "BADGE":
                getBadgeNotification(message);
                break;
            case "VOTE":
                getVoteNotification(message);
                break;
        }

        if (builder != null) {
            manager.notify(MESSAGE_NOTIFICATION_ID, builder.build());
        }
    }

    private void getBadgeNotification(RemoteMessage message) {
        Intent i = new Intent("badge_event");
        i.putExtra("extra_badge_title", message.getData().get("badge_title"));
        i.putExtra("extra_badge_image", message.getData().get("badge_image"));
        i.putExtra("extra_badge_content", message.getData().get("badge_content"));

        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private NotificationCompat.Builder getCommentNotification(RemoteMessage message) {
        Intent i = new Intent(this, CommentActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("post_id", message.getData().get("post_id"));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

        mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_comment, message.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_forum_white_24dp)
                .setContentIntent(pendingIntent);
        return mBuilder;
    }

    private NotificationCompat.Builder getVoteNotification(RemoteMessage message) {
        Intent i = new Intent(this, CommentActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("post_id", message.getData().get("post_id"));
        i.putExtra("user_id", message.getData().get("user_id"));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

        mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_vote, message.getData().get("username")))
                .setSmallIcon(R.drawable.ic_forum_white_24dp);
        return mBuilder;
    }

    private NotificationCompat.Builder getFollowNotification(RemoteMessage message) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(ProfileActivity.EXTRA_USERNAME, message.getNotification().getBody());
        i.putExtra(ProfileActivity.EXTRA_USER_ID, message.getData().get("user_id"));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

        mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_follow, message.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_b_white_24dp)
                .setContentIntent(pendingIntent);
        return mBuilder;
    }
}
