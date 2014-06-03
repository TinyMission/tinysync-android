package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.SaveResult;
import com.tinymission.tinysync.sync.SyncEntity;
import com.tinymission.tinysync.sync.SyncRequest;
import com.tinymission.tinysync.sync.Syncer;

import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import models.Author;
import models.MyContext;

public class SyncTests extends AndroidTestCase {

    MyContext _context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _context = new MyContext(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        _context.destroySchema();
        _context.close();
    }

    @Test
    public void testRequest() {
        final int numAuthors = 2;
        for (int i=0; i<numAuthors; i++) {
            Author author = new Author();
            author.name = "Author " + i;
            _context.authors.add(author);
        }
        _context.save();

        Syncer syncer = new Syncer(_context);
        SyncRequest request = syncer.generateRequest();

        SyncEntity authorEntity = request.getEntities()[0];
        assertEquals(numAuthors, authorEntity.getCreated().length);
    }

    @Test
    public void testResponse() {
        try {
            InputStream stream = getContext().getAssets().open("response1.json");
            Syncer syncer = new Syncer(_context);
            SyncRequest response = syncer.parseJson(stream);
            stream.close();

            assertEquals(0, DateTime.parse("2014-02-14T09:12:43-0700").compareTo(response.getLastSynced()));
            assertEquals(2, response.getEntities().length);

            SyncEntity authorEntity = response.getEntities()[0];
            assertEquals("author", authorEntity.getName());
            DbModel[] created = authorEntity.parseCreated(_context.authors);
            assertEquals(1, created.length);
            Author author1 = (Author) created[0];
            assertEquals("Author 1", author1.name);
            assertEquals(42, author1.age);
            assertEquals(Author.Seniority.senior, author1.seniority);

            SaveResult result = syncer.processResponse(response);

            assertEquals(3, result.getInserted().size());

        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


}