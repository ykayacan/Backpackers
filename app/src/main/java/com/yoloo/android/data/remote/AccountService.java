package com.yoloo.android.data.remote;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import com.yoloo.android.Constants;
import com.yoloo.android.backend.modal.yolooApi.YolooApi;
import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.local.AccountRealm;

import java.io.IOException;

import rx.Observable;
import rx.functions.Func0;

public class AccountService {

    private static YolooApi sYolooApi = null;

    public Observable<Account> createAccount(final AccountRealm item) {
        return Observable.defer(new Func0<Observable<Account>>() {
            @Override
            public Observable<Account> call() {
                if (sYolooApi == null) {  // Only do this once
                    sYolooApi = new YolooApi.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(), null)
                            .setRootUrl(Constants.API_BASEURL)
                            .build();
                }

                try {
                    Account account = sYolooApi.addAccount(item.getAccessToken()).execute();
                    return Observable.just(account);
                } catch (IOException e) {
                    return Observable.error(e);
                }
            }
        });

        /*return Observable.create(new Observable.OnSubscribe<Account>() {
            @Override
            public void call(Subscriber<? super Account> subscriber) {
                if (subscriber.isUnsubscribed()) return;
                if (sYolooApi == null) {  // Only do this once
                    sYolooApi = new YolooApi.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new AndroidJsonFactory(), null)
                            .setRootUrl(Constants.API_BASEURL)
                            .build();
                }

                try {
                    subscriber.onNext(sYolooApi.addAccount(model.getAccessToken()).execute());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }

            }
        });*/
    }

}
