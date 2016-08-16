package com.yoloo.android.backend.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.AbstractPost;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.location.Location;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class RemoveTimelineServlet extends HttpServlet {

    private static final String WEBSAFE_POST_ID = "websafePostId";

    public static void create(String websafePostId) {
        Queue queue = QueueFactory.getQueue("remove-timeline-queue");
        queue.add(TaskOptions.Builder
                .withUrl("/tasks/removeTimeline")
                .param(WEBSAFE_POST_ID, websafePostId));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String websafePostId = req.getParameter(WEBSAFE_POST_ID);

        final Key<? extends AbstractPost> postKey = Key.create(websafePostId);

        List<Key<TimelineFeed>> feedKeys = ofy().load()
                .type(TimelineFeed.class).filter("postKey =", postKey).keys().list();

        List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().list();

        List<Key<Like>> likeKeys = ofy().load().type(Like.class)
                .filter("likeableEntityKey =", postKey).keys().list();

        List<Object> deleteList = ImmutableList.builder()
                .addAll(feedKeys)
                .addAll(locationKeys)
                .addAll(likeKeys)
                .add(postKey)
                .build();

        ofy().delete().entities(deleteList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
