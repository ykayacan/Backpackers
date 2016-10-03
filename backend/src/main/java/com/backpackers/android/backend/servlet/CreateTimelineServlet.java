package com.backpackers.android.backend.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.googlecode.objectify.Key;
import com.backpackers.android.backend.model.feed.TimelineFeed;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.follow.Follow;
import com.backpackers.android.backend.model.user.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class CreateTimelineServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(CreateTimelineServlet.class.getSimpleName());

    private static final String WEBSAFE_USER_ID = "websafeUserId";
    private static final String WEBSAFE_POST_ID = "websafePostId";
    private static final String CREATED_AT = "createdAt";

    public static void create(String websafeUserId,
                              String websafePostId,
                              String createdAt) {
        Queue queue = QueueFactory.getQueue("create-timeline-queue");
        queue.add(TaskOptions.Builder
                .withUrl("/tasks/createTimeline")
                .param(WEBSAFE_USER_ID, websafeUserId)
                .param(WEBSAFE_POST_ID, websafePostId)
                .param(CREATED_AT, createdAt));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
        final String websafeUserId = req.getParameter(WEBSAFE_USER_ID);
        final String websafePostId = req.getParameter(WEBSAFE_POST_ID);
        final String createdAt = req.getParameter(CREATED_AT);

        final Key<Account> userKey = Key.create(websafeUserId);
        final Key<AbstractPost> postKey = Key.create(websafePostId);

        // Get current user's follower keys.
        final List<Key<Account>> followerKeys = getFollowerKeys(userKey);

        final List<TimelineFeed> feeds = new ArrayList<>(followerKeys.size());

        final Date date = new Date(Long.parseLong(createdAt));

        // Add post key to each followee timeline.
        for (Key<Account> followerKey : followerKeys) {
            feeds.add(TimelineFeed.newInstance(followerKey, postKey, date));
        }

        ofy().save().entities(feeds);
    }

    private List<Key<Account>> getFollowerKeys(Key<Account> userKey) {
        // Get user's followers.
        List<Key<Follow>> followKeys = ofy().load().type(Follow.class)
                .filter("followeeKey =", userKey).keys().list();

        List<Key<Account>> followerKeys = new ArrayList<>(followKeys.size());
        for (Key<Follow> followKey : followKeys) {
            followerKeys.add(followKey.<Account>getParent());
        }

        return followerKeys;
    }
}
