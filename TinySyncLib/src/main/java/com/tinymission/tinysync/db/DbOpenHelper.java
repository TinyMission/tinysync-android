package com.tinymission.tinysync.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper to facilitate opening the database.
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    DbContext _dbContext;

    public DbOpenHelper(DbContext dbContext) {
        super(dbContext.getAndroidContext(), dbContext.getDatabaseName(), null, 1);
        _dbContext = dbContext;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        _dbContext.updateSchema(db);
    }
}
