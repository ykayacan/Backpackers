package com.backpackers.android.backend.util;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;

import com.backpackers.android.backend.notification.PushBody;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class NotificationHelper {

    public static final Gson GSON = new Gson();
    private static final Logger logger =
            Logger.getLogger(NotificationHelper.class.getName());
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
    private static URLFetchService service = URLFetchServiceFactory.getURLFetchService();

    public static void sendCommentNotification(String regId, String content, String websafePostId) {
        final PushBody body = new PushBody();
        body.setTo(regId)
                .addNotification("body", content)
                .addData("action", "COMMENT")
                .addData("post_id", websafePostId);

        sendNotification(body);
    }

    public static void sendFollowNotification(String regId, String username, String userId) {
        final PushBody body = new PushBody();
        body.setTo(regId)
                .addNotification("body", username)
                .addData("action", "FOLLOW")
                .addData("user_id", userId);

        sendNotification(body);
    }

    public static void sendAchievementNotification(String regId, String title, String image,
                                                   String content) {
        final PushBody body = new PushBody();
        body.setTo(regId)
                .addData("action", "BADGE")
                .addData("badge_title", title)
                .addData("badge_image", image)
                .addData("badge_content", content);

        sendNotification(body);
    }

    public static void sendVoteUpNotification(String regId, String postId,
                                              String senderId, String senderName) {
        final PushBody body = new PushBody();
        body.setTo(regId)
                .addData("action", "VOTE")
                .addData("post_id", postId)
                .addData("username", senderName)
                .addData("user_id", senderId);

        sendNotification(body);
    }

    private static void sendNotification(PushBody body) {
        String json = GSON.toJson(body);

        HTTPRequest request = null;
        URL url;
        try {
            url = new URL(FCM_ENDPOINT);
            request = new HTTPRequest(url, HTTPMethod.POST);

            request.addHeader(new HTTPHeader("Authorization", "key=" + System.getProperty("gcm.api.key")));
            request.addHeader(new HTTPHeader("Content-Type", "application/json"));
            request.setPayload(json.getBytes());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        service.fetchAsync(request);
    }
}
