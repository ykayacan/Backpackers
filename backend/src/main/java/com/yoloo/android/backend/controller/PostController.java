package com.yoloo.android.backend.controller;

import com.google.api.client.util.Strings;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.AbstractPost;
import com.yoloo.android.backend.model.feed.post.NormalPost;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

import java.util.Date;
import java.util.List;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

abstract class PostController {

    private List<Key<Location>> getLocationKeys(Key<? extends AbstractPost> postKey) {
        return ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().list();
    }

    void updateLocations(AbstractPost abstractPost, String locations) {
        if (!Strings.isNullOrEmpty(locations)) {
            final Key<? extends AbstractPost> postKey = abstractPost.getKey();

            // Delete all entities.
            ofy().delete().keys(getLocationKeys(postKey));

            if (abstractPost instanceof NormalPost) {
                ((NormalPost) abstractPost).setLocations(LocationHelper.getLocationSet(locations, postKey));
            } else if (abstractPost instanceof ForumPost) {
                ((ForumPost) abstractPost).setLocations(LocationHelper.getLocationSet(locations, postKey));
            }
        }
    }

    void updateHashtags(AbstractPost abstractPost, String hashtags) {
        if (!Strings.isNullOrEmpty(hashtags)) {
            if (abstractPost instanceof NormalPost) {
                ((NormalPost) abstractPost).getHashtags().clear();
                ((NormalPost) abstractPost).getHashtags().addAll(StringUtil.split(hashtags, ","));
            } else if (abstractPost instanceof ForumPost) {
                ((ForumPost) abstractPost).getHashtags().clear();
                ((ForumPost) abstractPost).getHashtags().addAll(StringUtil.split(hashtags, ","));
            }
        }
    }

    void updateContent(AbstractPost abstractPost, String content) {
        if (!Strings.isNullOrEmpty(content)) {
            abstractPost.setContent(content);
        }
    }

    void updateDate(AbstractPost abstractPost) {
        if (abstractPost instanceof NormalPost) {
            ((NormalPost) abstractPost).setUpdatedAt(new Date());
        } else if (abstractPost instanceof ForumPost) {
            ((ForumPost) abstractPost).setUpdatedAt(new Date());
        }
    }

    void updateAccepted(ForumPost post, Boolean isAccepted) {
        if (isAccepted != null) {
            post.setAccepted(isAccepted);
        }
    }

    void updateAward(ForumPost post, Key<Account> userKey, Integer awardRep) {
        if (awardRep != null) {
            post.setAwardedByWebsafeId(userKey);
            post.setAwardRep(awardRep);
        }
    }

    void updateLocked(ForumPost post, Boolean isLocked) {
        if (isLocked != null) {
            post.setLocked(isLocked);
        }
    }

    void updateMedia(AbstractPost abstractPost) {
    }

}
