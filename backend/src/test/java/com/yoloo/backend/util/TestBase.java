package com.yoloo.backend.util;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;

import java.util.logging.Logger;

public class TestBase extends GaeTestBase {

    @SuppressWarnings("unused")
    protected static Logger log = Logger.getLogger(TestBase.class.getName());

    private Closeable session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = ObjectifyService.begin();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        session.close();
        super.tearDown();
    }
}
