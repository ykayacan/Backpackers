package com.yoloo.android.backend.factory.post;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.AbstractPost;
import com.yoloo.android.backend.model.feed.post.NormalPost;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

public class NormalPostFactory implements PostAbstractFactory {

    private final Key<NormalPost> postKey;
    private final Account account;
    private final String content;
    private final String hashtags;
    private final String location;

    public NormalPostFactory(Key<NormalPost> postKey, Account account,
                             String content, String hashtags, String location) {
        this.postKey = postKey;
        this.account = account;
        this.content = content;
        this.hashtags = hashtags;
        this.location = location;
    }

    @Override
    public AbstractPost create() {
        return NormalPost.builder()
                .setHashtags(StringUtil.split(hashtags, ","))
                .setLocations(LocationHelper.getLocationSet(location, postKey))
                /*.setVideoUrl()*/
                .setKey(postKey)
                .setParentUserKey(account.getKey())
                .setUsername(account.getUsername())
                .setProfileImageUrl(account.getProfileImageUrlLink())
                .setContent(content)
                .build();
    }
}
