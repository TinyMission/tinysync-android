package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;
import org.junit.Test;

import models.MyContext;

public class DbTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext();
    }

    @Test
    public void testInitialization() {
        _context.initialize();

        assertEquals(3, _context.getDbSets().size());
    }

}