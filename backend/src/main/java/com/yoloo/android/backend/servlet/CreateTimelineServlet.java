package com.yoloo.android.backend.servlet;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.user.Account;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String websafeUserId = req.getParameter(WEBSAFE_USER_ID);
        final String websafePostId = req.getParameter(WEBSAFE_POST_ID);
        final String createdAt = req.getParameter(CREATED_AT);

        final Key<Account> userKey = Key.create(websafeUserId);
        final Key<TimelinePost> postKey = Key.create(websafePostId);

        // Get current user's follower keys.
        final List<Key<Account>> followerKeys = getFollowerKeys(userKey);

        final List<TimelineFeed<TimelinePost>> feeds =
                new ArrayList<>(followerKeys.size());

        final Date date = new Date(Long.parseLong(createdAt));

        // Add post key to each followee timeline.
        for (Key<Account> followerKey : followerKeys) {
            feeds.add(TimelineFeed.newInstance(followerKey, postKey, date));
        }

        ofy().save().entities(feeds);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private List<Key<Account>> getFollowerKeys(Key<Account> userKey) {
        // Get user's followers
        Query<Follow> query =
                ofy().load().type(Follow.class).filter("followeeKey =", userKey);

        QueryResultIterator<Follow> queryIterator = query.iterator();
        List<Key<Account>> followerKeys = new ArrayList<>(queryIterator.getIndexList().size());
        while (queryIterator.hasNext()) {
            followerKeys.add(queryIterator.next().getParentUserKey());
        }

        return followerKeys;
    }
}
