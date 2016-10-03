package com.backpackers.android.internal;

import rx.Observable;
import rx.subjects.PublishSubject;

public class RxBus {

    private static RxBus sRxBus;

    private PublishSubject<Object> subject = PublishSubject.create();

    public static RxBus getRxBus() {
        if (sRxBus == null) {
            sRxBus = new RxBus();
        }
        return sRxBus;
    }

    /**
     * Pass any event down to event listeners.
     */
    public void setString(Object object) {
        subject.onNext(object);
    }

    /**
     * Subscribe to this Observable. On event, do something
     * e.g. replace a fragment
     */
    public Observable<Object> getEvents() {
        return subject;
    }
}
