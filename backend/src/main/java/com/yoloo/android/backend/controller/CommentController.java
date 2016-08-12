package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.counter.Count;
import com.yoloo.android.backend.counter.question.QuestionCommentCounter;
import com.yoloo.android.backend.model.comment.Comment;
import com.yoloo.android.backend.model.like.Like;
import com.yoloo.android.backend.model.question.Question;
import com.yoloo.android.backend.model.question.QuestionCounter;
import com.yoloo.android.backend.model.user.Account;

import java.util.List;
import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class CommentController {

    private static final Logger logger = Logger.getLogger(CommentController.class.getSimpleName());

    public static CommentController newInstance() {
        return new CommentController();
    }

    public Comment add(final String questionWebsafeKey,
                       final String commentText,
                       final User user) {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<Question> questionKey = Key.create(questionWebsafeKey);

        Account account = ofy().load().type(Account.class).id(userKey.getId()).now();

        QuestionCounter counter =
                ofy().load().type(QuestionCounter.class).ancestor(questionKey).first().now();

        Count count = new QuestionCommentCounter(counter);
        count.increase();

        Comment comment = buildComment(commentText, userKey, questionKey, account);

        ofy().save().entities(comment, counter).now();
        return comment;
    }

    public void remove(String questionWebsafeKey,
                       String commentWebsafeKey,
                       User user) {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<Question> questionKey = Key.create(questionWebsafeKey);
        Key<Comment> commentKey = Key.create(commentWebsafeKey);
        List<Key<Like>> likeKeys = ofy().load().type(Like.class)
                .ancestor(userKey).filter("likeableEntity =", commentKey).keys().list();

        QuestionCounter counter = ofy().load().type(QuestionCounter.class)
                .ancestor(questionKey).first().now();

        Count count = new QuestionCommentCounter(counter);
        count.decrease();

        ofy().save().entity(counter).now();

        ofy().delete().keys(likeKeys);
        ofy().delete().keys(commentKey);
    }

    private Comment buildComment(String commentText,
                                 Key<Account> userKey,
                                 Key<Question> questionKey,
                                 Account account) {
        return Comment.builder(questionKey, userKey)
                .setComment(commentText)
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrl())
                .build();
    }
}
