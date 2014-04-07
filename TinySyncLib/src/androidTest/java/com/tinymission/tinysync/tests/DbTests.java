package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.DbSet;
import com.tinymission.tinysync.db.SaveResult;

import org.joda.time.DateTime;
import org.junit.Test;

import models.Author;
import models.MyContext;
import models.Post;

public class DbTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext(getContext());
        _context.destroySchema();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        _context.close();
    }

    @Test
    public void testSchema() {
        _context.initialize();

        assertEquals(3, _context.getCollections().size());

        assertEquals(1, _context.authors.getHasManies().size());
        assertEquals("author_id", _context.authors.getHasManies().values().iterator().next().getForeignKey());
        assertEquals(1, _context.posts.getBelongsTos().size());
        assertEquals("author_id", _context.posts.getBelongsTos().values().iterator().next().getColumnName());

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
        bob.seniority = Author.Seniority.senior;
        _context.authors.add(bob);
        _context.save();

        Author bob2 = _context.authors.find(bob.id);
        assertEquals(bob.name, bob2.name);
        assertTrue(bob2.isPersisted());
        assertEquals(DbModel.SyncState.infant, bob2.syncState);
        assertEquals("Bob Johnson", bob2.name);
        assertEquals(42, bob2.age);
        assertEquals(Author.Seniority.senior, bob2.seniority);
        assertEquals(0, DateTime.now().getMillis() - bob2.createdAt.getMillis(), 50);
        assertEquals(0, DateTime.now().getMillis() - bob2.updatedAt.getMillis(), 20);

    }

    @Test
    public void testBelongsTo() {
        Author bob = new Author();
        bob.name = "Bob Johnson";
        _context.authors.add(bob);
        _context.save();

        Post firstPost = new Post();
        firstPost.author.setValue(bob);
        firstPost.title = "My First Post";
        _context.posts.add(firstPost);
        _context.save();

        Post readPost = _context.posts.find(firstPost.id);
        assertEquals(bob.id, readPost.author.getKey());
        Author bob2 = readPost.author.getValue(_context);
        assertEquals(bob.id, bob2.id);
    }

    @Test
    public void testHasMany() {
        Author bob = new Author();
        bob.name = "Bob Johnson";
        _context.authors.add(bob);

        int numPosts = 4;
        for (int i=0; i<numPosts; i++) {
            Post post = new Post();
            post.author.setValue(bob);
            post.title = "Post " + i;
            _context.posts.add(post);
        }

        _context.save();

        DbSet<Post> posts = bob.posts.getValues(_context);
        assertEquals(numPosts, posts.size());

    }
}