package com.backpackers.android.backend.util;

import com.googlecode.objectify.cmd.Query;

public class SearchUtil {

    // TODO: 16.09.2016 Ignore case.
    public static  <T> Query<T> fieldStartsWith(Query<T> query, String field, String search) {
        query = query.filter(field + " >=", search);
        return query.filter(field + " <", search + "\ufffd");
    }
}
