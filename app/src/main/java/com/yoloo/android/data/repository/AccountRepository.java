package com.yoloo.android.data.repository;

import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.Mapper;
import com.yoloo.android.data.Specification;
import com.yoloo.android.data.local.AccountRealm;
import com.yoloo.android.data.model.AccountModel;
import com.yoloo.android.data.remote.AccountService;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountRepository extends Repository<AccountRealm> {

    private final RealmConfiguration mConfiguration;
    private final AccountService mService;
    private final Mapper<Account, AccountModel> mMapper;

    public AccountRepository(RealmConfiguration configuration, AccountService service) {
        mConfiguration = configuration;
        mService = service;
        mMapper = new Mapper<Account, AccountModel>() {
            @Override
            public AccountModel map(Account account) {
                return null;
            }
        };
    }

    @Override
    public void create(AccountRealm item) {
        saveToLocalDb(item);
        sendToServer(item);
    }

    @Override
    public void update(AccountRealm item) {

    }

    @Override
    public void delete(AccountRealm item) {

    }

    @Override
    public Observable<List<AccountRealm>> query(Specification specification) {
        return null;
    }

    private void saveToLocalDb(final AccountRealm item) {
        final Realm realm = Realm.getInstance(mConfiguration);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(item);
            }
        });
        realm.close();
    }

    private void sendToServer(final AccountRealm item) {
        mService.createAccount(item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
