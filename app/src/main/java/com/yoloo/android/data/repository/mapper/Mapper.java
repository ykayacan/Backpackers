package com.yoloo.android.data.repository.mapper;

public interface Mapper<From, To> {
    To map(From from);
}
