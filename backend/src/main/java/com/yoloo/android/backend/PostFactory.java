package com.yoloo.android.backend;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.AdsPost;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

import java.util.List;

public class PostFactory {

    public TimelinePost createTimelinePost(Key<TimelinePost> postKey,
                                           Account account,
                                           String content,
                                           String hashtags,
                                           String location) {
        // TODO: 10.08.2016 Add mediaId case.

        // Generate hashtags from given string with immutable way.
        List<String> hastagList =
                ImmutableList.copyOf(StringUtil.splitValueByToken(hashtags, ","));

        // Generate locations from given string with immutable way.
        List<Location> locationList = LocationHelper.getLocationList(location, postKey);

        return TimelinePost.builder()
                .setHashtags(hastagList)
                .setLocations(locationList)
                /*.setVideoUrl()*/
                .setKey(postKey)
                .setParentUserKey(account.getKey())
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrlLink())
                .setContent(content)
                .build();
    }

    public AdsPost createAdsPost() {
        return null;
    }

    public ForumPost createForumPost(Key<ForumPost> postKey,
                                     Account account,
                                     String content,
                                     String hashtags,
                                     String locations,
                                     Boolean isLocked,
                                     Integer awardRep) {
        // TODO: 10.08.2016 Add mediaId case.

        // Generate hashtags from given string with immutable way.
        List<String> hastagList =
                ImmutableList.copyOf(StringUtil.splitValueByToken(hashtags, ","));

        // Generate locations from given string with immutable way.
        List<Location> locationList = LocationHelper.getLocationList(locations, postKey);

        return ForumPost.builder()
                .setHashtags(hastagList)
                .setLocations(locationList)
                .setLocked(isLocked)
                /*.setVideoUrl()*/
                .setAward(account.getKey(), awardRep)
                .setKey(postKey)
                .setParentUserKey(account.getKey())
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrlLink())
                .setContent(content)
                .build();
    }
}
