package com.backpackers.android.backend.model.like;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.backpackers.android.backend.model.user.Account;

@Entity
public class Like {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    private Key<? extends Likeable> likeableEntityKey;

    private Like() {
    }

    private Like(final Key<Account> likedByKey,
                 final Key<? extends Likeable> likeableKey) {
        this.parentUserKey = likedByKey;
        this.likeableEntityKey = likeableKey;
    }

    public static Like with(final Key<Account> likedByKey,
                            final Key<? extends Likeable> likeableKey) {
        return new Like(likedByKey, likeableKey);
    }
}
