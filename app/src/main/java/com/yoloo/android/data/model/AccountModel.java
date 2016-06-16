package com.yoloo.android.data.model;

public class AccountModel {
    private String mRealname;
    private String mUsername;
    private String mEmail;
    private String mPictureUrl;
    private String mAccessToken;

    private AccountModel(String realname, String username, String email, String pictureUrl, String accessToken) {
        mRealname = realname;
        mUsername = username;
        mEmail = email;
        mPictureUrl = pictureUrl;
        mAccessToken = accessToken;
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

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public static final class Builder {
        private String mRealname;
        private String mUsername;
        private String mEmail;
        private String mPictureUrl;
        private String mAccessToken;

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

        public Builder setPictureUrl(String pictureUrl) {
            mPictureUrl = pictureUrl;
            return this;
        }

        public Builder setAccessToken(String accessToken) {
            mAccessToken = accessToken;
            return this;
        }

        public AccountModel build() {
            return new AccountModel(mRealname, mUsername, mEmail, mPictureUrl, mAccessToken);
        }
    }
}
