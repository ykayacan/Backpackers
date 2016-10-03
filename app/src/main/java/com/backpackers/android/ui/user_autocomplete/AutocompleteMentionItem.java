package com.backpackers.android.ui.user_autocomplete;

import com.backpackers.android.backend.modal.yolooApi.model.Account;

public class AutocompleteMentionItem {

    private Account mAccount;

    public AutocompleteMentionItem(Account account) {
        mAccount = account;
    }

    public Account getAccount() {
        return mAccount;
    }

    @Override
    public String toString() {
        return "@" + mAccount.getUsername();
    }
}
