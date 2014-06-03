package com.tinymission.tinysync.tests;

import android.test.AndroidTestCase;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.tinymission.tinysync.util.ParamsParser;

import org.junit.Test;

import models.MyContext;

/**
 * Tests classes in the util package.
 */
public class UtilTests  extends AndroidTestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }



    @Test
    public void testParamsParser() {
        String params = "where[user_id]=qwe123&where[age]=42&limit=10";
        JsonObject json = new ParamsParser(params).toJson();

        JsonObject where = json.getAsJsonObject("where");
        assertNotNull(where);
        assertEquals("qwe123", where.getAsJsonPrimitive("user_id").getAsString());
        assertEquals(42, where.getAsJsonPrimitive("age").getAsInt());

        JsonPrimitive limit = json.getAsJsonPrimitive("limit");
        assertNotNull(limit);
        assertEquals(10, limit.getAsInt());
    }
}
