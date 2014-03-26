package com.tinymission.tinysync.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Joiner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for database contexts.
 * A database context provides an interface for querying and storing model objects.
 * <p>
 * To implement a context for your application, simply extend DbContext and add public DbSet fields
 * for each type of DbModel you're using.
 */
public abstract class DbContext {

    private static final String LogTag = "tinysync.db.DbContext";

    private ArrayList<DbSet> _sets = new ArrayList<DbSet>();
    /**
     * @return the values of the DbSet fields in this context
     */
    public List<DbSet> getDbSets() {
        return _sets;
    }

    private String _databaseName;
    /**
     * @return the name of the database this context uses for persistence
     */
    public String getDatabaseName() {
        return _databaseName;
    }

    private Context _androidContext;
    /**
     * @return the android context used to obtain a database connection
     */
    public Context getAndroidContext() {
        return _androidContext;
    }

    DbOpenHelper _openHelper;

    public DbContext(Context androidContext) {
        _databaseName = getClass().getSimpleName();
        _androidContext = androidContext;
        _openHelper = new DbOpenHelper(this);
    }


    //region Initialization

    private boolean _isInitialized = false;

    /**
     * Initializes the context by parsing the DbSets it contains.
     * This will get lazily called before the first query, but you can call it explicitly if you want.
     */
    public void initialize() {
        if (_isInitialized)
            return;

        try {
            for (Field field: getClass().getFields()) {
                if (DbSet.class.isAssignableFrom(field.getType())) {
                    DbSet set = (DbSet)field.get(this);
                    Log.v(LogTag, "Added DbSet for " + set.getTableName() + " to context " + getClass().getSimpleName());
                    _sets.add(set);
                }
            }
        }
        catch (IllegalAccessException ex) {
            throw new RuntimeException("Error initializing database context: " + ex.getMessage());
        }

        _isInitialized = true;
    }

    /**
     * Opens the database connection (if it isn't open already) and makes sure the schema is up to date.
     * This can be called in the background or at a time of your choosing to avoid having it happen
     * automatically when the first query is run.
     */
    public void touch() {
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        db.close();
    }


    boolean doesTableExist(SQLiteDatabase db, String tableName) {
        String query = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?;";
        Cursor cursor = db.rawQuery(query, new String[] {tableName});
        if (!cursor.moveToFirst())
            return false;
        return cursor.getInt(0) > 0;
    }

    /**
     * Updates the schema of the database to ensure it's the same as that of the models.
     * If no database is present, one will be created with the current model schema.
     */
    public void updateSchema(SQLiteDatabase db) {
        for (DbSet set: _sets) {
            String tableName = set.getTableName();
            if (!doesTableExist(db, tableName)) {
                List<String> columnDefs = set.getColumnDefs();
                String columnStatement = Joiner.on(", ").join(columnDefs);
                Log.d(LogTag, "Table " + tableName + " does not exist, creating it with " + columnStatement);
                db.execSQL("CREATE TABLE " + tableName + "(" + columnStatement + ")");
                if (!doesTableExist(db, tableName))
                    throw new RuntimeException("WTF, table still doesn't exist!");
            }
            else {
                Log.d(LogTag, "Table " + tableName + " already exists");
            }
        }
    }

    public void destroySchema() {

    }

    //endregion


    //region Persistence

    public SaveResult save() {
        initialize();
        SaveResult result = new SaveResult();
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        for (DbSet set: _sets) {
            result.mergeFrom(set.save(this, db));
        }
        db.close();
        return result;
    }

    //endregion

}
