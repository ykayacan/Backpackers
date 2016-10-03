package com.backpackers.android.data.mapper;

public interface Mapper<From, To> {

    To map(From from);
}
