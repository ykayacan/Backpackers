package com.backpackers.backend.util;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;

class GaeTestBase {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setApplyAllHighRepJobPolicy());

    @Before
    public void setUp() throws Exception {
        this.helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        this.helper.tearDown();
    }

}
