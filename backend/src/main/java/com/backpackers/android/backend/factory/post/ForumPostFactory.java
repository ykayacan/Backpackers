package com.backpackers.android.backend.factory.post;

import com.google.api.client.util.Strings;

import com.backpackers.android.backend.model.feed.post.ForumPost;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.util.LocationHelper;
import com.googlecode.objectify.Key;
import com.backpackers.android.backend.model.feed.post.AbstractPost;
import com.backpackers.android.backend.model.user.Account;

import java.util.List;
import java.util.Map;

public class ForumPostFactory implements PostAbstractFactory {

    private final Key<? extends AbstractPost> postKey;
    private final Account account;
    private final String content;
    private final List<String> hashTags;
    private final String location;
    private final Boolean isLocked;
    private final Integer awardRep;
    private final Map<Key<Media>, Media> mediaMap;

    public ForumPostFactory(Key<ForumPost> postKey, Account account,
                            String content, List<String> hashTags, String location,
                            Map<Key<Media>, Media> mediaMap,
                            Boolean isLocked, Integer awardRep) {
        this.postKey = postKey;
        this.account = account;
        this.content = content;
        this.hashTags = hashTags;
        this.location = location;
        this.isLocked = isLocked;
        this.awardRep = awardRep;
        this.mediaMap = mediaMap;
    }

    @Override
    public AbstractPost create() {
        return ForumPost.builder()
                .setHashTags(hashTags)
                .setLocation(Strings.isNullOrEmpty(location)
                        ? null : LocationHelper.getLocationFromString(location, postKey))
                .setLocked(isLocked)
                .setAward(account.getKey(), awardRep)
                .setMedias(mediaMap == null ? null : mediaMap.values())
                .setKey(postKey)
                .setParentUserKey(account.getKey())
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrlLink())
                .setContent(content)
                .build();
    }
}
