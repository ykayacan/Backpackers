package com.backpackers.android.backend.servlet;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.googlecode.objectify.Key;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.model.vote.Vote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class RemoveUserServlet extends HttpServlet {

    private static final String WEBSAFE_USER_ID = "websafeUserId";

    public static void create(String websafePostId) {
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder
                .withUrl("/tasks/removeUser")
                .param(WEBSAFE_USER_ID, websafePostId));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String userId = req.getParameter(WEBSAFE_USER_ID);

        final Key<Account> userKey = Key.create(userId);

        final List<Key<ForumPost>> postKeys =
                ofy().load().type(ForumPost.class).ancestor(userKey).keys().list();

        List<QueryResultIterable<? extends Key>> deleteKeysIterable = new ArrayList<>();
        for (Key<ForumPost> key : postKeys) {
            QueryResultIterable<Key<Location>> locationIterable =
                    ofy().load().type(Location.class).filter("postKey =", key).keys().iterable();
            QueryResultIterable<Key<Vote>> voteIterable =
                    ofy().load().type(Vote.class).filter("postKey =", key).keys().iterable();
            QueryResultIterable<Key<Media>> mediaIterable = ofy().load().type(Media.class)
                    .filter("websafePostId =", key.toWebSafeString()).keys().iterable();

            deleteKeysIterable.add(locationIterable);
            deleteKeysIterable.add(voteIterable);
            deleteKeysIterable.add(mediaIterable);
        }

        /*List<Key<?>> keys = new ArrayList<>(deleteKeysIterable.size());
        for (deleteKeysIterable.iterator().hasNext()) {
            keys.add(deleteKeysIterable.iterator().next().iterator().next());
        }*/

        //ofy().delete().keys(keys);
    }
}
