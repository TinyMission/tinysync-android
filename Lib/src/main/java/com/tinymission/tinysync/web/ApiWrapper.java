package com.tinymission.tinysync.web;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbContext;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.DbSet;
import com.tinymission.tinysync.db.SaveResult;
import com.tinymission.tinysync.query.Query;

/**
 * Javascript wrapper for the TinySync Javascript API.
 */
public class ApiWrapper {

    final String LogTag = "tinysync.web.ApiWrapper";

    private DbContext _context;

    public ApiWrapper(DbContext dbContext) {
        _context = dbContext;
    }

    @JavascriptInterface
    public ApiResponse get(String collectionName, String rawQuery) {
        Log.v(LogTag, "API get on collection " + collectionName + " with query " + rawQuery);
        DbCollection collection = _context.getCollection(collectionName);
        if (collection == null)
            return ApiResponse.error(collectionName, "Unknown collection");
        Query query = Query.fromJson(collection, rawQuery);
        try {
            DbSet results = collection.runQuery(query);
            return ApiResponse.success(collection.getTableName(), "Successfully executed query", results.toArray());
        }
        catch (Exception ex) {
            Log.w(LogTag, "Error executing get on " + collection.getTableName(), ex);
            return ApiResponse.error(collection.getTableName(), "Error executing query: " + ex.getMessage());
        }
    }

    @JavascriptInterface
    public ApiResponse create(String collectionName, String rawRecord) {
        Log.v(LogTag, "API create on collection " + collectionName + " with record " + rawRecord);
        DbCollection collection = _context.getCollection(collectionName);
        if (collection == null)
            return ApiResponse.error(collectionName, "Unknown collection");
        try {
            DbModel record = collection.fromJson(rawRecord);
            collection.add(record);
            SaveResult result = _context.save();
            if (result.getInserted().size() == 1)
                return ApiResponse.success(collectionName, "Successfully inserted record", result.getInserted().toArray(new DbModel[1]));
            else if (result.getErrored().size() > 0) {
                DbModel errored = result.getErrored().iterator().next();
                return ApiResponse.error(collectionName, "Error inserting record: " + errored.getErrorMessage());
            } else {
                return ApiResponse.error(collectionName, "Error inserting record");
            }
        }
        catch (Exception ex) {
            return ApiResponse.error(collectionName, "Error inserting record: " + ex.getMessage());
        }
    }

}
