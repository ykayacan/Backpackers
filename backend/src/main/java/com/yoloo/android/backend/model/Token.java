package com.yoloo.android.backend.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.oauth2.OAuth;

import java.util.Date;

@Entity
@Cache
public class Token {

    @Id
    private Long id;

    @Parent
    private Key<Account> userKey;

    @Index
    private String accessToken;

    @Index
    private String refreshToken;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Date createdAt;

    // Extra fields

    @Ignore
    private String tokenType = OAuth.OAUTH_HEADER_NAME;

    @Ignore
    private long expiresIn = Constants.TOKEN_EXPIRES_IN;

    private Token() {
    }

    private Token(Builder builder) {
        this.userKey = builder.userKey;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.createdAt = new Date();
    }

    public static Token.Builder builder(Key<Account> userKey) {
        return new Token.Builder(userKey);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Token> getKey() {
        return Key.create(userKey, Token.class, id);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isTokenExpired() {
        long expireInSec = Constants.TOKEN_EXPIRES_IN * 1000;
        long currentTime = System.currentTimeMillis();
        return expireInSec + createdAt.getTime() < currentTime;
    }

    public static final class Builder {
        private Key<Account> userKey;
        private String accessToken;
        private String refreshToken;

        public Builder(Key<Account> userKey) {
            this.userKey = userKey;
        }

        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }
}
