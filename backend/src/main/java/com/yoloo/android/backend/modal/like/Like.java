package com.yoloo.android.backend.modal.like;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Deletable;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.Date;

import lombok.Getter;

@Entity
@Cache
public abstract class Like implements Deletable {

    @Id
    @Getter
    private Long id;

    @Index
    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> accountRef;

    @Index
    @Getter
    protected Date createdAt;

    Like() {
    }

    Like(Key<Account> accountKey) {
        this.accountRef = Ref.create(accountKey);
        this.createdAt = new Date();
    }

    public Account getAccount() {
        return this.accountRef.get();
    }

    @Override
    public Key<Account> getAccountKey() {
        return this.accountRef.key();
    }
}