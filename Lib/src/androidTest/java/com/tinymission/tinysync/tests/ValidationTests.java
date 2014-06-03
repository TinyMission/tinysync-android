package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import org.junit.Test;

import models.Author;
import models.MyContext;

public class ValidationTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext(getContext());
        _context.destroySchema();
    }

    @Test
    public void testNotNull() {
        Author unnamed = new Author();
        assertFalse(_context.authors.validate(unnamed));
        assertEquals(1, unnamed.getErrors().size());
        assertEquals("must not be null", unnamed.getErrors().get(0).getMessage());
    }



}
