package com.yoloo.android.backend.controller;

import com.google.api.client.util.Strings;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

import java.util.Date;
import java.util.List;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

abstract class PostController {

    private List<Key<Location>> getLocationKeys(Key<? extends Post> postKey) {
        return ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().list();
    }

    void updateLocations(Post post, String locations) {
        if (!Strings.isNullOrEmpty(locations)) {
            final Key<? extends Post> postKey = post.getKey();

            // Delete all entities.
            ofy().delete().keys(getLocationKeys(postKey));

            if (post instanceof TimelinePost) {
                ((TimelinePost) post).setLocations(LocationHelper.getLocationSet(locations, postKey));
            } else if (post instanceof ForumPost) {
                ((ForumPost) post).setLocations(LocationHelper.getLocationSet(locations, postKey));
            }
        }
    }

    void updateHashtags(Post post, String hashtags) {
        if (!Strings.isNullOrEmpty(hashtags)) {
            if (post instanceof TimelinePost) {
                ((TimelinePost) post).getHashtags().clear();
                ((TimelinePost) post).getHashtags().addAll(StringUtil.split(hashtags, ","));
            } else if (post instanceof ForumPost) {
                ((ForumPost) post).getHashtags().clear();
                ((ForumPost) post).getHashtags().addAll(StringUtil.split(hashtags, ","));
            }
        }
    }

    void updateContent(Post post, String content) {
        if (!Strings.isNullOrEmpty(content)) {
            post.setContent(content);
        }
    }

    void updateDate(Post post) {
        if (post instanceof TimelinePost) {
            ((TimelinePost) post).setUpdatedAt(new Date());
        } else if (post instanceof ForumPost) {
            ((ForumPost) post).setUpdatedAt(new Date());
        }
    }

    void updateAccepted(ForumPost post, Boolean isAccepted) {
        if (isAccepted != null) {
            post.setAccepted(isAccepted);
        }
    }

    void updateAward(ForumPost post, Key<Account> userKey, Integer awardRep) {
        if (awardRep != null) {
            post.setAwardedBy(userKey);
            post.setAwardRep(awardRep);
        }
    }

    void updateLocked(ForumPost post, Boolean isLocked) {
        if (isLocked != null) {
            post.setLocked(isLocked);
        }
    }

    void updateMedia(Post post) {
    }

}
