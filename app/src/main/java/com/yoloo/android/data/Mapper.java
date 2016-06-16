package com.yoloo.android.data;

public interface Mapper<From, To> {
    To map(From from);
}
