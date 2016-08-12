package com.yoloo.android.backend.util;

public final class ClassUtil {

    // See details: http://stackoverflow.com/a/5756536/3154765
    @SuppressWarnings("unchecked")
    public static <T> Class<T> castClass(Class<?> aClass) {
        return (Class<T>)aClass;
    }
}
