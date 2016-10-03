package com.backpackers.android.backend.controller;

import com.google.api.client.util.Strings;

import com.backpackers.android.backend.model.hashtag.HashTag;
import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.util.LocationHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.feed.post.NormalPost;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

abstract class PostController {

    private Key<Location> getLocationKey(Key<? extends AbstractPost> postKey) {
        return ofy().load().type(Location.class)
                .filter("postKey =", postKey).keys().first().now();
    }

    void updateLocation(AbstractPost abstractPost, String location) {
        if (!Strings.isNullOrEmpty(location)) {
            final Key<? extends AbstractPost> postKey = abstractPost.getKey();

            // Delete all entities.
            ofy().delete().key(getLocationKey(postKey));

            if (abstractPost instanceof NormalPost) {
                ((NormalPost) abstractPost).setLocation(LocationHelper.getLocationFromString(location, postKey));
            } else if (abstractPost instanceof ForumPost) {
                ((ForumPost) abstractPost).setLocation(LocationHelper.getLocationFromString(location, postKey));
            }
        }
    }

    List<HashTag> updateHashTags(AbstractPost abstractPost, String hashtags) {
        if (!Strings.isNullOrEmpty(hashtags)) {
            Query<HashTag> query = ofy().load().type(HashTag.class);

            List<String> bareHashTags = StringUtil.split(hashtags, ",");

            for (String bareHashTag : bareHashTags) {
                query.filter("hashTag =", bareHashTag);
            }

            ofy().delete().keys(query.keys().list());

            List<HashTag> hashTags = new ArrayList<>(bareHashTags.size());
            for (String bareHashTag : bareHashTags) {
                hashTags.add(new HashTag(bareHashTag));
            }

            if (abstractPost instanceof NormalPost) {
                ((NormalPost) abstractPost).getHashtags().clear();
                ((NormalPost) abstractPost).getHashtags().addAll(bareHashTags);
            } else if (abstractPost instanceof ForumPost) {
                ((ForumPost) abstractPost).getHashtags().clear();
                ((ForumPost) abstractPost).getHashtags().addAll(bareHashTags);
            }

            return hashTags;
        }

        return null;
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

    void updateMedia(AbstractPost abstractPost, String mediaIds) {
        if (!Strings.isNullOrEmpty(mediaIds)) {
            final List<Key<Media>> mediaKeys = new ArrayList<>(3);
            final List<String> mediaIdList = StringUtil.splitValueByToken(mediaIds, ",");

            for (String mediaId : mediaIdList) {
                mediaKeys.add(Key.<Media>create(mediaId));
            }
            Map<Key<Media>, Media> mediaMap = ofy().load().keys(mediaKeys);

            final String websafePostKey = abstractPost.getWebsafeId();
            for (Media media : mediaMap.values()) {
                media.setWebsafePostId(websafePostKey);
            }

            ofy().save().entities(mediaMap.values());

            abstractPost.setMedias(mediaMap.values());
        }
    }

}
