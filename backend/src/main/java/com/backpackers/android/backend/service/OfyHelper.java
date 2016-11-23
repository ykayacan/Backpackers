package com.backpackers.android.backend.service;

import com.backpackers.android.backend.badge.EntrepreneurBadge;
import com.backpackers.android.backend.badge.FirstUserBadge;
import com.backpackers.android.backend.badge.NewbieBadge;
import com.backpackers.android.backend.badge.WastedBadge;
import com.backpackers.android.backend.badge.WelcomeBadge;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.model.Token;
import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.feed.TimelineFeed;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.feed.post.NormalPost;
import com.backpackers.android.backend.model.follow.Follow;
import com.backpackers.android.backend.model.hashtag.HashTag;
import com.backpackers.android.backend.model.like.Like;
import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.model.user.UserCounterShard;
import com.backpackers.android.backend.model.user.UserIndexShardCounter;
import com.backpackers.android.backend.model.vote.Vote;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper {

    static {
        factory().register(Token.class);
        factory().register(Account.class);
        factory().register(UserCounterShard.class);
        factory().register(UserIndexShardCounter.class);
        //factory().register(PostCounterShard.class);
        factory().register(Location.class);
        factory().register(RegistrationRecord.class);
        factory().register(Media.class);
        factory().register(Follow.class);
        factory().register(Comment.class);
        factory().register(Vote.class);
        factory().register(Like.class);
        factory().register(ForumPost.class);
        factory().register(NormalPost.class);
        factory().register(TimelineFeed.class);
        factory().register(HashTag.class);
        factory().register(Notification.class);

        factory().register(EntrepreneurBadge.class);
        factory().register(FirstUserBadge.class);
        factory().register(NewbieBadge.class);
        factory().register(WastedBadge.class);
        factory().register(WelcomeBadge.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
