package com.tinymission.tinysync.tests;

import android.test.ActivityTestCase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.tinymission.tinysync.db.SaveResult;

import org.junit.Test;

import models.Author;
import models.MyContext;

public class DbTests extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MyContext context = new MyContext(getContext());
    }

    @Test
    public void testInitialization() {
        Log.v("DbTests", "android context: " + getContext().getClass().getSimpleName());
        MyContext context = new MyContext(getContext());
        context.initialize();

        assertEquals(3, context.getDbSets().size());


        context.touch();
    }

    @Test
    public void testCreation() {
        MyContext context = new MyContext(getContext());

        Author author = new Author();
        author.name = "Bob Johnson";
        author.age = 42;
        context.authors.add(author);

        SaveResult result = context.save();
        assertEquals(1, result.getInserted().size());
        assertEquals(0, result.getErrored().size());
    }

}