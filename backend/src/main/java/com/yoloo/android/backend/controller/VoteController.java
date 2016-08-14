package com.yoloo.android.backend.controller;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.vote.Vote;

import java.util.logging.Logger;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class VoteController {

    private static final Logger logger =
            Logger.getLogger(VoteController.class.getName());

    public static VoteController newInstance() {
        return new VoteController();
    }

    public void vote(final String websafePostId, final int direction, final User user) {
        Key<Account> userKey = Key.create(user.getUserId());
        Key<ForumPost> postKey = Key.create(websafePostId);

        switch (direction) {
            case -1:
                downVote(userKey, postKey);
                break;
            case 0:
                defaultVote(userKey, postKey);
                break;
            case 1:
                upVote(userKey, postKey);
                break;
            default:
                defaultVote(userKey, postKey);
                break;
        }
    }

    private void upVote(final Key<Account> userKey, final Key<ForumPost> postKey) {
        runTransact(userKey, postKey, Vote.Status.UP);
    }

    private void defaultVote(final Key<Account> userKey, final Key<ForumPost> postKey) {
        runTransact(userKey, postKey, Vote.Status.DEFAULT);
    }

    private void downVote(final Key<Account> userKey, final Key<ForumPost> postKey) {
        runTransact(userKey, postKey, Vote.Status.DOWN);
    }

    private Vote getVote(Key<Account> userKey, Key<ForumPost> postKey) {
        Vote vote;

        try {
            vote = ofy().load().type(Vote.class)
                    .ancestor(userKey).filter("postKey =", postKey)
                    .first().safe();
        } catch (NotFoundException e) {
            vote = Vote.with(userKey, postKey);
        }

        return vote;
    }

    private void runTransact(final Key<Account> userKey,
                             final Key<ForumPost> postKey,
                             final Vote.Status status) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                Vote vote = getVote(userKey, postKey);

                vote.setStatus(status);
                ofy().save().entity(vote);
            }
        });
    }
}
