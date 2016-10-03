package com.backpackers.android.backend.factory.user;

import com.backpackers.android.backend.Config;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.UserUtil;
import com.googlecode.objectify.Key;

public class YolooUserFactory implements AbstractUserFactory {

    private final Key<Account> userKey;
    private final String[] values;
    private final String locale;

    public YolooUserFactory(Key<Account> userKey, String[] values, String locale) {
        this.userKey = userKey;
        this.values = values;
        this.locale = locale;
    }

    @Override
    public Account create() {
        return Account.builder(userKey)
                .setUsername(UserUtil.setUsername(values[0]))
                .setPassword(values[1])
                .setEmail(values[2])
                .setProvider(Account.Provider.YOLOO)
                .setProfileImageUrl(Config.DUMMY_PROFILE_IMAGE)
                .build();
    }
}
