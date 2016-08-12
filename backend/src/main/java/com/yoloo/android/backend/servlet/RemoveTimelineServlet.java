package com.yoloo.android.backend.servlet;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.util.ClassUtil;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class RemoveTimelineServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String websafePostId = req.getParameter("websafePostId");

        final Key<TimelinePost> postKey = Key.create(websafePostId);

        List<Key<TimelineFeed<TimelinePost>>> feedKeys = ofy().load()
                        .type(ClassUtil.<TimelineFeed<TimelinePost>>castClass(TimelineFeed.class))
                        .filter("postKey =", postKey).keys().list();

        List<Key<Location>> locationKeys = ofy().load().type(Location.class)
                        .filter("postKey =", postKey).keys().list();

        List<Object> deleteList = ImmutableList.builder()
                .addAll(feedKeys)
                .addAll(locationKeys)
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
