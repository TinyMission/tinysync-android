package com.tinymission.tinysync.tests;

import android.test.ActivityTestCase;
import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Test;

import models.MyContext;

public class DbTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testInitialization() {
        Log.v("DbTests", "android context: " + getContext().getClass().getSimpleName());
        _context = new MyContext(getContext());
        _context.initialize();

        assertEquals(3, _context.getDbSets().size());


        _context.touch();
    }

}