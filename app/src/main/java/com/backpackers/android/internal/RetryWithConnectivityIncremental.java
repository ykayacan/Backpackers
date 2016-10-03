package com.backpackers.android.internal;

import android.content.Context;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class RetryWithConnectivityIncremental implements
        Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int maxTimeout;
    private final TimeUnit timeUnit;
    private final Observable<Boolean> isConnected;
    private final int startTimeOut;
    private int timeout;

    public RetryWithConnectivityIncremental(Context context, int startTimeOut, int maxTimeout, TimeUnit timeUnit) {
        this.startTimeOut = startTimeOut;
        this.maxTimeout = maxTimeout;
        this.timeUnit = timeUnit;
        this.timeout = startTimeOut;
        isConnected = getConnectedObservable(context);
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
            @Override
            public Observable<?> call(Throwable throwable) {
                if (throwable.getMessage().contains("")) {
                    return isConnected;
                } else {
                    return Observable.error(throwable);
                }
            }
        }).compose(new Observable.Transformer<Object, Object>() {
            @Override
            public Observable<Object> call(Observable<Object> objectObservable) {
                return (Observable<Object>) attachIncrementalTimeout();
            }
        });
    }

    private Observable.Transformer<Boolean, Boolean> attachIncrementalTimeout() {
        return new Observable.Transformer<Boolean, Boolean>() {
            @Override
            public Observable<Boolean> call(Observable<Boolean> observable) {
                return observable.timeout(timeout, timeUnit)
                        .doOnError(new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (throwable instanceof TimeoutException) {
                                    timeout = timeout > maxTimeout ? maxTimeout : timeout + startTimeOut;
                                }
                            }
                        });
            }
        };
    }

    private Observable<Boolean> getConnectedObservable(Context context) {
        return BroadcastObservable.fromConnectivityManager(context)
                .distinctUntilChanged()
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isConnected) {
                        return isConnected;
                    }
                });
    }
}
