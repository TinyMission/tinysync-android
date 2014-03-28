package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import com.tinymission.tinysync.db.DbSet;

import org.junit.Test;
import models.Author;
import models.MyContext;

/**
 * Tests the query interface.
 */
public class QueryTests extends AndroidTestCase {

    MyContext _context;

    static final int NUM_AUTHORS = 10;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext(getContext());
        _context.destroySchema();

        for (int n=0; n<NUM_AUTHORS; n++) {
            Author author = new Author();
            author.name = "Author " + n;
            author.age = n;
            _context.authors.add(author);
        }
        _context.save();
    }

    @Test
    public void testWhere() {

        DbSet<Author> authors = _context.authors.where("age", 5).run();
        assertEquals(1, authors.size());
        assertEquals(5, authors.first().age);
        assertEquals("Author 5", authors.first().name);

    }


}
