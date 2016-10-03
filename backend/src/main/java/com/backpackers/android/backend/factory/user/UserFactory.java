package com.backpackers.android.backend.factory.user;

import com.backpackers.android.backend.model.user.Account;

public class UserFactory {

    public static Account getAccount(AbstractUserFactory factory) {
        return factory.create();
    }
}
