package com.backpackers.android.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class BroadcastObservable implements Observable.OnSubscribe<Boolean> {

    private final Context mContext;

    public BroadcastObservable(Context context) {
        mContext = context;
    }

    public static Observable<Boolean> fromConnectivityManager(final Context context) {
        return Observable.create(new BroadcastObservable(context))
                .share();
    }

    @Override
    public void call(final Subscriber<? super Boolean> subscriber) {
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                subscriber.onNext(isConnectedToInternet());
            }
        };

        mContext.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        subscriber.add(unSubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                mContext.unregisterReceiver(receiver);
            }
        }));
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static Subscription unSubscribeInUiThread(final Action0 unSubscribe) {
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    unSubscribe.call();
                } else {
                    final Scheduler.Worker inner = AndroidSchedulers.mainThread().createWorker();
                    inner.schedule(new Action0() {
                        @Override
                        public void call() {
                            unSubscribe.call();
                            inner.unsubscribe();
                        }
                    });
                }
            }
        });
    }
}
