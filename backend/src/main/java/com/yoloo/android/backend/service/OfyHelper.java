package com.yoloo.android.backend.service;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.yoloo.android.backend.model.Photo;
import com.yoloo.android.backend.model.RegistrationRecord;
import com.yoloo.android.backend.model.Token;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.feed.TimelineFeed;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.follow.Follow;
import com.yoloo.android.backend.model.like.LikeEntity;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.media.Media;
import com.yoloo.android.backend.model.media.MediaToken;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.model.question.QuestionCounter;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.user.UserIndexShard;
import com.yoloo.android.backend.model.user.UserIndexShardCounter;

/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run before a JSP is run.  This is
 * required to let JSP's access Ofy.
 **/
public class OfyHelper {

    static {
        factory().register(Token.class);
        factory().register(Account.class);
        factory().register(UserIndexShard.class);
        factory().register(UserIndexShardCounter.class);
        factory().register(Comment.class);
        factory().register(com.yoloo.android.backend.model.like.Like.class);
        factory().register(Question.class);
        factory().register(QuestionCounter.class);
        factory().register(Location.class);
        factory().register(Photo.class);
        factory().register(RegistrationRecord.class);
        factory().register(Follow.class);
        factory().register(MediaToken.class);
        factory().register(Media.class);
        factory().register(LikeEntity.class);
        factory().register(ForumPost.class);
        factory().register(TimelinePost.class);
        factory().register(TimelineFeed.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
