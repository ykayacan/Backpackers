package com.yoloo.android.backend.model.like;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.user.Account;

@Entity
public class LikeEntity<T extends Likeable> {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<T> likeableEntityKey;

    private LikeEntity() {
    }

    private LikeEntity(final Key<Account> likedByKey,
                       final Key<T> likeableEntityKey) {
        this.parentUserKey = likedByKey;
        this.likeableEntityKey = likeableEntityKey;
    }

    public static <T extends Likeable> LikeEntity<T> with(final Key<Account> likedByKey,
                                                      final Key<T> likeableEntityKey) {
        return new LikeEntity<>(likedByKey, likeableEntityKey);
    }
}
