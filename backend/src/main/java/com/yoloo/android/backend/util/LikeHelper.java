package com.yoloo.android.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.feed.post.TimelinePost;
import com.yoloo.android.backend.model.like.LikeEntity;
import com.yoloo.android.backend.model.user.Account;

import java.util.Collection;
import java.util.Map;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class LikeHelper {

    public static void setLikeCountByPost(Collection<TimelinePost> posts) {
        for (TimelinePost post : posts) {

            long count = ofy().load().type(ClassUtil.<LikeEntity<Post>>castClass(LikeEntity.class))
                    .filter("postKey =", post.getKey()).keys().list().size();
            post.setLikeCount(count);
        }
    }

    public static void setUserLikes(Collection<TimelinePost> posts,
                                    Map<Key<TimelinePost>,
                                            LoadResult<Key<LikeEntity<TimelinePost>>>> map) {
        for (TimelinePost post : posts) {
            LoadResult<Key<LikeEntity<TimelinePost>>> result = map.get(post.getKey());

            if (result.now() != null) {
                post.setLiked(true);
            }
        }
    }

    public static long getTotalPostLikesByUserKey(final Key<Account> userKey) {
        return ofy().load().type(ClassUtil.<LikeEntity<Post>>castClass(LikeEntity.class))
                .ancestor(userKey).keys().list().size();
    }
}
