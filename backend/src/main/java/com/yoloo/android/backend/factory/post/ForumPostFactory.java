package com.yoloo.android.backend.factory.post;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.ForumPost;
import com.yoloo.android.backend.model.feed.post.AbstractPost;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

public class ForumPostFactory implements PostAbstractFactory {

    private final Key<? extends AbstractPost> postKey;
    private final Account account;
    private final String content;
    private final String hashtags;
    private final String locations;
    private final Boolean isLocked;
    private final Integer awardRep;

    public ForumPostFactory(Key<ForumPost> postKey, Account account,
                            String content, String hashtags, String locations,
                            Boolean isLocked, Integer awardRep) {
        this.postKey = postKey;
        this.account = account;
        this.content = content;
        this.hashtags = hashtags;
        this.locations = locations;
        this.isLocked = isLocked;
        this.awardRep = awardRep;
    }

    @Override
    public AbstractPost create() {
        return ForumPost.builder()
                .setHashtags(StringUtil.split(hashtags, ","))
                .setLocations(LocationHelper.getLocationSet(locations, postKey))
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
