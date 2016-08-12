package com.yoloo.android.data.local;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(
        name = YolooDatabase.NAME,
        version = YolooDatabase.VERSION,
        foreignKeysSupported = YolooDatabase.FOREIGN_KEY_ENABLED
)
public class YolooDatabase {

    static final String NAME = "yoloo";
    static final int VERSION = 1;
    static final boolean FOREIGN_KEY_ENABLED = true;
}
