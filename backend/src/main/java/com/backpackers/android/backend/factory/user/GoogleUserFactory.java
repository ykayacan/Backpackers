package com.backpackers.android.backend.factory.user;

import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.backpackers.android.backend.util.UserUtil;

public class GoogleUserFactory implements AbstractUserFactory {

    private final Key<Account> userKey;
    private final GoogleIdToken.Payload payload;
    private final String locale;

    public GoogleUserFactory(Key<Account> userKey,
                             GoogleIdToken.Payload payload,
                             String locale) {
        this.userKey = userKey;
        this.payload = payload;
        this.locale = locale;
    }

    @Override
    public Account create() {
        return Account.builder(userKey)
                .setUsername(UserUtil.setUsername((String) payload.get("name")))
                .setEmail(payload.getEmail())
                .setLocale(locale)
                .setProvider(Account.Provider.GOOGLE)
                .setProfileImageUrl(UserUtil.setProfileImage((String) payload.get("picture")))
                .build();
    }
}
