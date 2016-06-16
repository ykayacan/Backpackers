package com.yoloo.android.backend.service;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.modal.Follow;
import com.yoloo.android.backend.modal.Hashtag;
import com.yoloo.android.backend.modal.Photo;
import com.yoloo.android.backend.modal.RegistrationRecord;
import com.yoloo.android.backend.modal.like.CommentLike;
import com.yoloo.android.backend.modal.like.FeedLike;
import com.yoloo.android.backend.modal.like.Like;
import com.yoloo.android.backend.modal.location.Location;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper implements ServletContextListener {

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    private static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // This will be invoked as part of a warmup request, or the first user request if no warmup
        // request.
        factory().register(Account.class);
        factory().register(Comment.class);
        factory().register(Like.class);
        factory().register(CommentLike.class);
        factory().register(FeedLike.class);
        factory().register(Feed.class);
        factory().register(Hashtag.class);
        factory().register(Location.class);
        factory().register(Photo.class);
        factory().register(RegistrationRecord.class);
        factory().register(Follow.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // App Engine does not currently invoke this method.
    }
}
