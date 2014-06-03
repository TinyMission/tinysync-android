package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import com.tinymission.tinysync.db.DbSet;
import com.tinymission.tinysync.query.OrderBy;
import com.tinymission.tinysync.query.Query;

import org.joda.time.DateTime;
import org.junit.Test;
import models.Author;
import models.MyContext;
import models.Post;

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

        for (int n=0; n<NUM_AUTHORS; n++) {
            Author author = new Author();
            author.name = "Author " + n;
            author.age = n;
            _context.authors.add(author);
        }
        _context.save();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        _context.destroySchema();
        _context.close();
    }

    @Test
    public void testWhere() {

        DbSet<Author> authors = _context.authors.where("age", 5).run();
        assertEquals(1, authors.size());
        assertEquals(5, authors.first().age);
        assertEquals("Author 5", authors.first().name);

        authors = _context.authors.where("age.gte", 5).orderBy("age", OrderBy.ASC).run();
        assertEquals(5, authors.size());
        assertEquals(5, authors.first().age);
        assertEquals("Author 5", authors.first().name);

        authors = _context.authors.orderBy("age", OrderBy.DESC).where("age neq", 5).run();
        assertEquals(9, authors.size());
        assertEquals(9, authors.first().age);

        authors = _context.authors.where("createdAt.lt", DateTime.now()).run();
        assertEquals(10, authors.size());

    }

    @Test
    public void testIncludes() {
        final int numPosts = 10;
        Author author = _context.authors.where("name", "Author 4").run().first();
        for (int i=0; i<numPosts; i++) {
            Post post = new Post();
            post.title = "Post " + i;
            post.author.setValue(author);
            _context.posts.add(post);
        }
        _context.save();

        author = _context.authors.where("name", "Author 4").include("posts").run().first();
        assertEquals(numPosts, author.posts.getCachedValues().size());

    }

    @Test
    public void testLimit() {
        final int limit = 2;
        DbSet<Author> authors = _context.authors.limit(limit).run();
        assertEquals(limit, authors.size());
    }

    @Test
    public void testJson() {
        final String json = "{\"where\": {\"name\": \"Author 5\"}, \"order\": {\"name\": \"asc\"}}";

        Query<Author> query = Query.fromJson(_context.authors, json);
        DbSet<Author> authors = _context.authors.runQuery(query);

        assertEquals(1, authors.size());
        assertEquals(5, authors.first().age);
        assertEquals("Author 5", authors.first().name);

        String json2 = query.toJson();
        assertEquals(json.toLowerCase().replaceAll("\\s+",""),
                json2.toLowerCase().replaceAll("\\s+",""));
    }


}
