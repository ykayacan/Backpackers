package com.backpackers.android.util;

import rx.Subscription;

public class RxUtil {

    public static void safelyUnbscribe(Subscription... subscriptions) {
        for (Subscription s : subscriptions) {
            if (s != null && !s.isUnsubscribed()) {
                s.unsubscribe();
            }
        }
    }
}
