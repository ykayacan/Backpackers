package com.yoloo.android.data;

import java.util.List;

public interface Repository<T> {
    void create(T item);

    void create(Iterable<T> items);

    void update(T item);

    void delete(T item);

    void delete(Specification specification);

    List<T> query(Specification specification);
}
