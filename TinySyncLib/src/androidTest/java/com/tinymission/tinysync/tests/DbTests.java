package com.tinymission.tinysync.tests;

import android.test.ActivityTestCase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tinymission.tinysync.db.SaveResult;

import org.junit.Test;

import models.Author;
import models.MyContext;

public class DbTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext(getContext());
        _context.destroySchema();
    }

    @Test
    public void testInitialization() {
        Log.v("DbTests", "android context: " + getContext().getClass().getSimpleName());
        _context.initialize();

        assertEquals(3, _context.getDbSets().size());


        _context.touch();
    }

    @Test
    public void testWriting() {

        Author bob = new Author();
        bob.name = "Bob Johnson";
        bob.age = 42;
        _context.authors.add(bob);

        Author jill = new Author();
        jill.name = "Jill Smith";
        jill.age = 34;
        _context.authors.add(jill);

        SaveResult result = _context.save();
        assertEquals(2, result.getInserted().size());
        assertEquals(0, result.getUpdated().size());
        assertEquals(0, result.getErrored().size());
        assertEquals(0, _context.authors.getNew().size());

        bob.name = "Robert Johnson";
        _context.authors.add(bob);

        result = _context.save();
        assertEquals(0, result.getInserted().size());
        assertEquals(1, result.getUpdated().size());
        assertEquals(0, result.getErrored().size());
        assertEquals(0, _context.authors.getChanged().size());

    }

    @Test
    public void testReading() {

        Author bob = new Author();
        bob.name = "Bob Johnson";
        bob.age = 42;
        _context.authors.add(bob);
        _context.save();

        Author bob2 = _context.authors.find(bob.id);
        assertEquals(bob.name, bob2.name);

    }

}