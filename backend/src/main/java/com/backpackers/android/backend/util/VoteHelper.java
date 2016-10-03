package com.backpackers.android.backend.util;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.model.vote.Vote;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class VoteHelper {

    public static ImmutableList<LoadResult<Key<Vote>>> loadAsyncVoteKeys(Key<Account> userKey,
                                                                         Key<? extends AbstractPost> postKey) {
        LoadResult<Key<Vote>> upVoteKey = ofy().load().type(Vote.class)
                .ancestor(userKey).filter("postKey =", postKey)
                .filter("status =", Vote.Status.UP)
                .keys().first();

        LoadResult<Key<Vote>> downVoteKey = ofy().load().type(Vote.class)
                .ancestor(userKey).filter("postKey =", postKey)
                .filter("status =", Vote.Status.DOWN)
                .keys().first();

        return ImmutableList.<LoadResult<Key<Vote>>>builder()
                .add(upVoteKey)
                .add(downVoteKey)
                .build();
    }

    public static void aggregateVotes(Collection<ForumPost> posts,
                                      Map<Key<? extends AbstractPost>, List<LoadResult<Key<Vote>>>> votedKeysMap) {
        for (ForumPost post : posts) {
            List<LoadResult<Key<Vote>>> loadResults = votedKeysMap.get(post.getKey());

            aggregateVotes(post, loadResults);
        }
    }

    public static void aggregateVotes(ForumPost post, List<LoadResult<Key<Vote>>> result) {
        Key<Vote> upVote = result.get(0).now();

        if (upVote == null) {
            Key<Vote> downVote = result.get(1).now();
            if (downVote == null) {
                post.setStatus(Vote.Status.DEFAULT);
            } else {
                post.setStatus(Vote.Status.DOWN);
            }
        } else {
            post.setStatus(Vote.Status.UP);
        }

        setVoteCount(post);
    }

    public static void setVoteCount(ForumPost post) {
        long upVotes = getVoteCount(post.getKey(), Vote.Status.UP);
        long downVotes = getVoteCount(post.getKey(), Vote.Status.DOWN);

        post.setUps(upVotes);
        post.setDowns(downVotes);
    }

    private static long getVoteCount(Key<? extends AbstractPost> postKey, Vote.Status status) {
        return ofy().load().type(Vote.class)
                .filter("postKey =", postKey)
                .filter("status =", status)
                .keys().list().size();
    }
}