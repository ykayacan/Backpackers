package com.yoloo.android.backend.factory.user;

import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.user.Account;

public class GoogleUserFactory implements AbstractUserFactory {

    private final Key<Account> userKey;
    private final GoogleIdToken.Payload payload;

    public GoogleUserFactory(Key<Account> userKey, GoogleIdToken.Payload payload) {
        this.userKey = userKey;
        this.payload = payload;
    }

    @Override
    public Account create() {
        return Account.builder(userKey)
                .setUsername((String) payload.get("name"))
                .setEmail(payload.getEmail())
                .setProvider(Account.Provider.GOOGLE)
                .setRealname((String) payload.get("given_name"))
                .setProfileImageUrl(payload.get("picture") != null ?
                        (String) payload.get("picture") : "https://s31.postimg.org/bgayiukkb/dummy_user_icon.jpg")
                .build();
    }
}
