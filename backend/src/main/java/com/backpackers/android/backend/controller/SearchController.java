package com.backpackers.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.model.comment.Comment;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.hashtag.HashTag;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.model.vote.Vote;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.util.CommentHelper;
import com.backpackers.android.backend.util.SearchUtil;
import com.backpackers.android.backend.util.VoteHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.model.feed.post.AbstractPost;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class SearchController {

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    private static final Logger logger =
            Logger.getLogger(SearchController.class.getName());

    /**
     * New instance user controller.
     *
     * @return the user controller
     */
    public static SearchController newInstance() {
        return new SearchController();
    }

    public CollectionResponse<Account> searchUsers(final String cursor,
                                                   Integer limit,
                                                   final String searchText,
                                                   final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<Account> query = OfyHelper.ofy().load().type(Account.class);

        query = SearchUtil.fieldStartsWith(query, "username", searchText);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<Account> queryIterator = query.iterator();

        final List<Account> accounts = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            accounts.add(queryIterator.next());
        }

        return CollectionResponse.<Account>builder()
                .setItems(accounts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    public CollectionResponse<HashTag> searchHashTags(final String cursor,
                                                      Integer limit,
                                                      final String hashTag,
                                                      final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<HashTag> query = OfyHelper.ofy().load().type(HashTag.class);

        query = SearchUtil.fieldStartsWith(query, "hashTag", hashTag);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<HashTag> queryIterator = query.iterator();

        final Set<HashTag> hashTags = new LinkedHashSet<>(limit);
        while (queryIterator.hasNext()) {
            hashTags.add(queryIterator.next());
        }

        return CollectionResponse.<HashTag>builder()
                .setItems(hashTags)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    public CollectionResponse<ForumPost> searchPosts(final String cursor,
                                                     Integer limit,
                                                     final String hashTag,
                                                     final User user) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(user.getUserId());

        Query<ForumPost> query = OfyHelper.ofy().load().type(ForumPost.class);

        query = SearchUtil.fieldStartsWith(query, "hashtags", hashTag);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<ForumPost> queryIterator = query.iterator();

        // Store async batch in a hashmap. LinkedHashMap preserve insertion order.
        final Map<Key<? extends AbstractPost>, List<LoadResult<Key<Vote>>>> votedKeysMap =
                new LinkedHashMap<>(limit);
        final Map<Key<? extends AbstractPost>, LoadResult<Key<Comment>>> commentKeysMap =
                new LinkedHashMap<>(limit);

        final List<ForumPost> posts = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            // Get post key.
            final ForumPost post = queryIterator.next();
            final Key<? extends AbstractPost> postKey = post.getKey();
            posts.add(post);

            votedKeysMap.put(postKey, VoteHelper.loadAsyncVoteKeys(userKey, postKey));
            commentKeysMap.put(postKey, CommentHelper.loadAsyncComments(userKey, postKey));
        }

        VoteHelper.aggregateVotes(posts, votedKeysMap);
        CommentHelper.aggregateComments(posts, commentKeysMap);

        return CollectionResponse.<ForumPost>builder()
                .setItems(posts)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}
