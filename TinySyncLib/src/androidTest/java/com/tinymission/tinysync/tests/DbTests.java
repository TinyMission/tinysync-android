package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;
import android.util.Log;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.SaveResult;

import org.joda.time.DateTime;
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
        _context.initialize();

        assertEquals(3, _context.getCollections().size());


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
        assertEquals(2, _context.authors.count());
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
        assertTrue(bob2.isPersisted());
        assertEquals(DbModel.SyncState.infant, bob2.syncState);
        assertEquals("Bob Johnson", bob2.name);
        assertEquals(42, bob2.age);
        assertEquals(0, DateTime.now().getMillis()-bob2.createdAt.getMillis(), 50);
        assertEquals(0, DateTime.now().getMillis()-bob2.updatedAt.getMillis(), 20);

    }

}