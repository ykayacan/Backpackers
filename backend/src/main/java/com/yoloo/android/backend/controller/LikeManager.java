package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.counter.comment.CommentLikeCounter;
import com.yoloo.android.backend.counter.question.QuestionLikeCounter;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.like.Likeable;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.model.question.QuestionCounter;
import com.yoloo.android.backend.model.user.Account;

import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeManager {

    private static final Logger logger = Logger.getLogger(LikeManager.class.getSimpleName());

    private final Class<? extends Likeable> type;
    private final Key<Account> userKey;
    private final String likeableEntityWebsafeKey;

    private LikeManager(Class<? extends Likeable> type,
                        String likeableEntityWebsafeKey,
                        User user) {
        this.type = type;
        this.likeableEntityWebsafeKey = likeableEntityWebsafeKey;
        this.userKey = Key.create(user.getUserId());
    }

    public static LikeManager newInstance(Class<? extends Likeable> type,
                                          String websafeKey,
                                          User user) {
        return new LikeManager(type, websafeKey, user);
    }

    public void like() {
        if (type == Question.class) {
            likeQuestion();
        } else if (type == Comment.class) {
            likeComment();
        }
    }

    public void unlike() {
        if (type == Question.class) {
            unlikeQuestion();
        } else if (type == Comment.class) {
            unlikeComment();
        }
    }

    private void likeQuestion() {
        Key<Question> questionKey = getLikeableKey();

        Like<Question> like = Like.builder(userKey, questionKey).build();

        QuestionCounter counter = ofy().load().type(QuestionCounter.class)
                .ancestor(questionKey).first().now();

        Count count = new QuestionLikeCounter(counter);
        count.increase();

        ofy().save().entities(like, counter).now();
    }

    private void unlikeQuestion() {
        Key<Question> questionKey = Key.create(likeableEntityWebsafeKey);

        Key<Like> likeKey = ofy().load().type(Like.class)
                .ancestor(userKey)
                .filter("likeableEntity =", questionKey)
                .keys().first().now();

        ofy().delete().key(likeKey);

        QuestionCounter counter = ofy().load().type(QuestionCounter.class)
                .ancestor(questionKey).first().now();

        Count count = new QuestionLikeCounter(counter);
        count.decrease();

        ofy().save().entity(counter).now();
    }

    private void likeComment() {
        Comment comment = getLikeableEntity(Comment.class, likeableEntityWebsafeKey);

        Like<Comment> like =
                Like.builder(userKey, comment.getKey()).build();

        Count count = new CommentLikeCounter(comment);
        count.increase();

        ofy().save().entities(like, comment).now();
    }

    private void unlikeComment() {
        Comment comment = getLikeableEntity(Comment.class, likeableEntityWebsafeKey);

        Key<Like> likeKey = ofy().load().type(Like.class)
                .ancestor(userKey)
                .filter("likeableEntity =", comment.getKey())
                .keys().first().now();

        ofy().delete().key(likeKey);

        Count count = new CommentLikeCounter(comment);
        count.decrease();

        ofy().save().entity(comment).now();
    }

    private <T> Key<T> getLikeableKey() {
        return Key.create(likeableEntityWebsafeKey);
    }

    private <T> T getLikeableEntity(Class<T> type, String likeableEntityWebsafeKey) {
        return ofy().load().type(type)
                .filter("__key__ =", Key.create(likeableEntityWebsafeKey)).first().now();
    }

}
