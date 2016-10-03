package com.backpackers.android.backend.servlet;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;

import com.backpackers.android.backend.algorithm.RankAlgorithm;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.util.VoteHelper;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpdateRankServlet extends HttpServlet {

    private static final int DEFAULT_LIMIT = 10000;

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        updateRankForPosts(null);
        resp.getWriter().println("UpdateRankTasks completed");
    }

    private void updateRankForPosts(String cursor) {
        Query<ForumPost> query = OfyHelper.ofy().load().type(ForumPost.class);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(DEFAULT_LIMIT);
        final QueryResultIterator<ForumPost> queryIterator = query.iterator();

        final List<ForumPost> posts = new ArrayList<>(DEFAULT_LIMIT);

        while (queryIterator.hasNext()) {
            // Get post key.
            final ForumPost post = queryIterator.next();

            VoteHelper.setVoteCount(post);

            post.setRank(RankAlgorithm.getHotRank(post.getUps(), post.getDowns(), post.getCreatedAt()));

            posts.add(post);
        }

        if (posts.isEmpty()) {
            return;
        }

        OfyHelper.ofy().save().entities(posts);

        //updateRankForPosts(queryIterator.getCursor().toWebSafeString());
    }
}
