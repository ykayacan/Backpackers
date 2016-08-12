package com.yoloo.android.data.model;

import java.io.File;

public class AccountModel {

    public static final int YOLOO = 0;
    public static final int FACEBOOK = 1;
    public static final int GOOGLE = 2;

    private String mRealname;
    private String mUsername;
    private String mEmail;
    private String mPassword;
    private String mPictureUrl;
    private File mPictureFile;
    private String mAccessToken;
    private int mAccountType;

    private AccountModel(Builder builder) {
        mRealname = builder.mRealname;
        mUsername = builder.mUsername;
        mEmail = builder.mEmail;
        mPassword = builder.mPassword;
        mPictureUrl = builder.mPictureUrl;
        mPictureFile = builder.mPictureFile;
        mAccessToken = builder.mAccessToken;
        mAccountType = builder.mAccountType;
    }

    public String getRealname() {
        return mRealname;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public File getPictureFile() {
        return mPictureFile;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public int getAccountType() {
        return mAccountType;
    }

    public static final class Builder {
        private String mRealname;
        private String mUsername;
        private String mEmail;
        private String mPassword;
        private String mPictureUrl;
        private File mPictureFile;
        private String mAccessToken;
        private int mAccountType;

        public Builder setRealname(String realname) {
            mRealname = realname;
            return this;
        }

        public Builder setUsername(String username) {
            mUsername = username;
            return this;
        }

        public Builder setEmail(String email) {
            mEmail = email;
            return this;
        }

        public Builder setPassword(String password) {
            mPassword = password;
            return this;
        }

        public Builder setPictureUrl(String pictureUrl) {
            mPictureUrl = pictureUrl;
            return this;
        }

        public Builder setPictureFile(File pictureFile) {
            mPictureFile = pictureFile;
            return this;
        }

        public Builder setAccessToken(String accessToken) {
            mAccessToken = accessToken;
            return this;
        }

        public Builder setAccountType(int type) {
            mAccountType = type;
            return this;
        }

        public AccountModel build() {
            return new AccountModel(this);
        }
    }
}
