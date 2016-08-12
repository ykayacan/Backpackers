package com.yoloo.android.data.repository.mapper;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.local.AccountTable;

public class AccountMapper implements Mapper<Account, AccountTable> {
    @Override
    public AccountTable map(Account account) {
        AccountTable table = new AccountTable();
        /*table.setEmail(account.getEmail().getEmail());
        table.setProfileImageUrl(account.getProfileImageUrl().getValue());*/
        return table;
    }
}
