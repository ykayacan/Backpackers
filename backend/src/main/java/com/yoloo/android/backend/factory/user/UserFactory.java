package com.yoloo.android.backend.factory.user;

import com.yoloo.android.backend.model.user.Account;

public class UserFactory {

    public static Account getAccount(AbstractUserFactory factory) {
        return factory.create();
    }
}
