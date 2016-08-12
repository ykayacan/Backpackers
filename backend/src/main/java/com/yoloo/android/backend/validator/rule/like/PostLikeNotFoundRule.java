package com.yoloo.android.backend.validator.rule.like;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.like.LikeEntity;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.ClassUtil;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class PostLikeNotFoundRule implements Rule<NotFoundException> {

    private final String websafePostId;
    private final User user;

    public PostLikeNotFoundRule(String websafePostId, User user) {
        this.websafePostId = websafePostId;
        this.user = user;
    }

    @Override
    public void validate() throws NotFoundException {
        try {
            Key<Account> userKey = Key.create(user.getUserId());
            Key<Post> postKey = Key.create(websafePostId);

            ofy().load()
                    .type(ClassUtil.<LikeEntity<Post>>castClass(LikeEntity.class))
                    .ancestor(userKey)
                    .filter("postKey =", postKey)
                    .keys().first().safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Like.");
        }
    }
}
