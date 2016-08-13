package com.yoloo.android.backend.util;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.vote.Vote;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class VoteHelper {

    public static ImmutableList<LoadResult<Key<Vote>>> loadAsyncVoteKeys(Key<Account> userKey,
                                                                         Key<? extends Post> postKey) {
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
                                      Map<Key<? extends Post>, List<LoadResult<Key<Vote>>>> votedKeysMap) {
        for (ForumPost post : posts) {
            List<LoadResult<Key<Vote>>> loadResults = votedKeysMap.get(post.getKey());

            aggregateVotes(post, loadResults);
            setVoteCount(post);
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
    }

    private static void setVoteCount(ForumPost post) {
        long upVotes = getVoteCount(post.getKey(), Vote.Status.UP);
        long downVotes = getVoteCount(post.getKey(), Vote.Status.DOWN);

        post.setUps(upVotes);
        post.setDowns(downVotes);
    }

    private static long getVoteCount(Key<? extends Post> postKey, Vote.Status status) {
        return ofy().load().type(Vote.class)
                .filter("postKey =", postKey)
                .filter("status =", status)
                .keys().list().size();
    }
}