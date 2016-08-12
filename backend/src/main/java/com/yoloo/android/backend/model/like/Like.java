package com.yoloo.android.backend.model.like;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;

@Entity
public class Like<T extends Likeable> {

    @Id
    protected Long id;

    @Parent
    @Load
    private Ref<Account> user;

    @Load
    private Ref<T> likeableEntity;

    @Index
    private Date createdAt;

    private Like() {
    }

    private Like(Builder<T> builder) {
        this.user = builder.user;
        this.likeableEntity = builder.likeableEntity;
        this.createdAt = new Date();
    }

    public static <T extends Likeable> Like.Builder<T> builder(Key<Account> userKey,
                                                               Key<T> likeableEntity) {
        return new Like.Builder<>(userKey, likeableEntity);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Key<T> getLikeableEntityKey() {
        return this.likeableEntity.getKey();
    }

    public static final class Builder<T extends Likeable> {
        private Ref<Account> user;
        private Ref<T> likeableEntity;

        public Builder(Key<Account> userKey, Key<T> entityKey) {
            this.user = Ref.create(userKey);
            this.likeableEntity = Ref.create(entityKey);
        }

        public Like<T> build() {
            return new Like<>(this);
        }
    }
}
