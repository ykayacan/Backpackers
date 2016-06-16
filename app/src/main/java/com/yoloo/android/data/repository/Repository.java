package com.yoloo.android.data.repository;

import com.yoloo.android.data.Specification;

import java.util.List;

import rx.Observable;

public abstract class Repository<M> {

    public abstract void create(M item);

    public void create(Iterable<M> items) {
    }

    public abstract void update(M item);

    public abstract void delete(M item);

    public void delete(Specification specification) {
    }

    public abstract Observable<List<M>> query(Specification specification);
}
