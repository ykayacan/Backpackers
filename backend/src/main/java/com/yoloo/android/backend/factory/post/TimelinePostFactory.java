package com.yoloo.android.backend.factory.post;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.LocationHelper;
import com.yoloo.android.backend.util.StringUtil;

public class TimelinePostFactory implements PostAbstractFactory {

    private final Key<? extends Post> postKey;
    private final Account account;
    private final String content;
    private final String hashtags;
    private final String location;

    public TimelinePostFactory(Key<? extends Post> postKey, Account account,
                               String content, String hashtags, String location) {
        this.postKey = postKey;
        this.account = account;
        this.content = content;
        this.hashtags = hashtags;
        this.location = location;
    }

    @Override
    public Post create() {
        return TimelinePost.builder()
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
