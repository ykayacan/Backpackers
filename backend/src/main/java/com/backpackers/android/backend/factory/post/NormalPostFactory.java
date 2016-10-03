package com.backpackers.android.backend.factory.post;

import com.google.api.client.util.Strings;

import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.LocationHelper;
import com.backpackers.android.backend.util.StringUtil;
import com.googlecode.objectify.Key;
import com.backpackers.android.backend.model.feed.post.NormalPost;

import java.util.Map;

public class NormalPostFactory implements PostAbstractFactory {

    private final Key<NormalPost> postKey;
    private final Account account;
    private final String content;
    private final String hashtags;
    private final String location;
    private final Map<Key<Media>, Media> mediaMap;

    public NormalPostFactory(Key<NormalPost> postKey, Account account,
                             String content, String hashtags, String location,
                             Map<Key<Media>, Media> mediaMap) {
        this.postKey = postKey;
        this.account = account;
        this.content = content;
        this.hashtags = hashtags;
        this.location = location;
        this.mediaMap = mediaMap;
    }

    @Override
    public AbstractPost create() {
        return NormalPost.builder()
                .setHashtags(StringUtil.split(hashtags, ","))
                .setLocation(Strings.isNullOrEmpty(location)
                        ? null : LocationHelper.getLocationFromString(location, postKey))
                .setMedias(mediaMap == null ? null : mediaMap.values())
                .setKey(postKey)
                .setParentUserKey(account.getKey())
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrlLink())
                .setContent(content)
                .build();
    }
}
