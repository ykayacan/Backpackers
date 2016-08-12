package com.yoloo.android.backend.model.media;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.android.backend.model.user.Account;

import java.util.Date;

@Entity
@Cache
public class Media {

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    private String fileName;

    private String contentType;

    private long size;

    private boolean isUploaded = false;

    @Index
    private Date createdAt;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    private Media() {
    }

    public static Media.Builder builder(Key<Account> parentUserKey) {
        return new Media.Builder(parentUserKey);
    }

    private Media(Builder builder) {
        this.parentUserKey = builder.parentUserKey;
        this.fileName = builder.fileName;
        this.contentType = builder.contentType;
        this.size = builder.size;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public static final class Builder {
        private Key<Account> parentUserKey;
        private String fileName;
        private String contentType;
        private long size;

        public Builder(Key<Account> parentUserKey) {
            this.parentUserKey = parentUserKey;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Media build() {
            return new Media(this);
        }
    }
}
