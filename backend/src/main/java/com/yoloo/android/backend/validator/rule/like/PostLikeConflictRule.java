package com.yoloo.android.backend.validator.rule.like;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.like.LikeEntity;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.ClassUtil;
import com.yoloo.android.backend.validator.Rule;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class PostLikeConflictRule implements Rule<ConflictException> {

    private final String websafePostId;
    private final User user;

    public PostLikeConflictRule(String websafePostId, User user) {
        this.websafePostId = websafePostId;
        this.user = user;
    }

    @Override
    public void validate() throws ConflictException {
        final Key<Account> userKey = Key.create(user.getUserId());
        final Key<Post> postKey = Key.create(websafePostId);

        final Key<LikeEntity<Post>> key = ofy().load()
                .type(ClassUtil.<LikeEntity<Post>>castClass(LikeEntity.class))
                .ancestor(userKey)
                .filter("postKey =", postKey)
                .keys().first().now();

        if (key != null) {
            throw new ConflictException("Already liked.");
        }
    }
}
