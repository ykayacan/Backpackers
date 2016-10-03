package com.backpackers.android.backend.mapper;

public interface Mapper<From, To> {

    To map(From from);
}
