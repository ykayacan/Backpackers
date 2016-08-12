package com.yoloo.android.data.repository;

import com.yoloo.android.data.repository.specification.Specification;

import java.util.List;

import rx.Observable;

public interface Repository<M> {

    void create(M item);

    void update(M item);

    void delete(M item);

    void delete(Specification specification);

    Observable<List<M>> query(Specification specification);
}
