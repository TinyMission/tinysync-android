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
 * To implement a context for your application, simply extend DbContext and add public DbCollection fields
 * for each type of DbModel you're using.
 */
public abstract class DbContext {

    private static final String LogTag = "tinysync.db.DbContext";

    private ArrayList<DbCollection> _collections = new ArrayList<DbCollection>();
    /**
     * @return the values of the DbCollection fields in this context
     */
    public List<DbCollection> getCollections() {
        return _collections;
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
                if (DbCollection.class.isAssignableFrom(field.getType())) {
                    DbCollection collection = (DbCollection)field.get(this);
                    collection.setContext(this);
                    Log.v(LogTag, "Added DbCollection for " + collection.getTableName() + " to context " + getClass().getSimpleName());
                    _collections.add(collection);
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
        for (DbCollection collection: _collections) {
            String tableName = collection.getTableName();
            if (!doesTableExist(db, tableName)) {
                List<String> columnDefs = collection.getColumnDefs();
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

    /**
     * Drops all tables mapped by this context.
     */
    public void destroySchema() {
        initialize();
        SQLiteDatabase db = getWritableDatabase();
        for (DbCollection set: _collections) {
            Log.d(LogTag, "Dropping table " + set.getTableName());
            db.execSQL("DROP TABLE IF EXISTS " + set.getTableName());
        }
        db.close();
    }

    /**
     * @param modelClass the model class to get the collection for
     * @return the collection corresponding to the model class
     */
    public <T extends DbModel> DbCollection<T> getCollection(Class<T> modelClass) {
        for (DbCollection<?> collection: _collections) {
            if (collection.getModelClass().equals(modelClass))
                return (DbCollection<T>) collection;
        }
        throw new InvalidCollectionException(modelClass.getSimpleName());
    }

    //endregion


    //region Connections

    private SQLiteDatabase _readableDb;

    public SQLiteDatabase getReadableDatabase() {
        if (_readableDb == null)
            _readableDb = _openHelper.getReadableDatabase();
        return _readableDb;
    }

    private SQLiteDatabase _writableDb;

    public SQLiteDatabase getWritableDatabase() {
        if (_writableDb == null)
            _writableDb = _openHelper.getWritableDatabase();
        return _writableDb;
    }

    /**
     * Closes any open database connections.
     * This should be called when the context is no longer needed.
     * It is safe to call this method multiple times.
     */
    public void close() {
        if (_readableDb != null) {
            _readableDb.close();
            _readableDb = null;
        }
        if (_writableDb != null) {
            _writableDb.close();
            _writableDb = null;
        }
    }

    //endregion


    //region Persistence

    /**
     * Saves all new or changed records that have been added to any of the context's DbSets.
     * @return a SaveResult containing information about the save operation
     */
    public SaveResult save() {
        initialize();
        SaveResult result = new SaveResult();
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        for (DbCollection collection: _collections) {
            result.mergeFrom(collection.save(this, db));
        }
        db.close();
        return result;
    }

    //endregion


    //region Exceptions

    public static class InvalidCollectionException extends RuntimeException {
        public InvalidCollectionException(String collectionName) {
            super("Collection " + collectionName + " does not exist in this context");
        }
    }

    public static class InvalidRelationshipException extends RuntimeException {
        public InvalidRelationshipException(String rel, String name) {
            super("No " + rel + " relationship found for " + name);
        }
    }

    //endregion
}
