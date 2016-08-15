package com.yoloo.android.backend.factory.user;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.user.Account;

public class YolooUserFactory implements AbstractUserFactory {

    private final Key<Account> userKey;
    private final String[] values;

    public YolooUserFactory(Key<Account> userKey, String[] values) {
        this.userKey = userKey;
        this.values = values;
    }

    @Override
    public Account create() {
        return Account.builder(userKey)
                .setUsername(values[0])
                .setPassword(values[1])
                .setEmail(values[2])
                .setProvider(Account.Provider.YOLOO)
                .setProfileImageUrl("https://s31.postimg.org/bgayiukkb/dummy_user_icon.jpg")
                .build();
    }
}
