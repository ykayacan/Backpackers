package com.yoloo.android.data.repository;

import com.yoloo.android.data.model.AccountModel;
import com.yoloo.android.data.repository.specification.Specification;

import java.util.List;

import rx.Observable;

public class AccountRepository implements Repository<AccountModel> {

    public AccountRepository() {
    }

    @Override
    public void create(AccountModel item) {

    }

    @Override
    public void update(AccountModel item) {

    }

    @Override
    public void delete(AccountModel item) {

    }

    @Override
    public void delete(Specification specification) {

    }

    @Override
    public Observable<List<AccountModel>> query(final Specification specification) {
        return null;
    }
}
