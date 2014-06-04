package com.tinymission.tinysync.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
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
        getWritableDatabase();
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
     * This is an extremely destructive and remorseless method, use with caution.
     */
    public void destroySchema() {
        initialize();
        SQLiteDatabase db = getWritableDatabase();
        for (DbCollection set: _collections) {
            Log.d(LogTag, "Dropping table " + set.getTableName());
            db.execSQL("DROP TABLE IF EXISTS " + set.getTableName());
        }
    }

    /**
     * @param modelClass the model class to get the collection for
     * @return the collection corresponding to the model class
     */
    public <T extends DbModel> DbCollection<T> getCollection(Class<T> modelClass) {
        initialize();
        for (DbCollection<?> collection: _collections) {
            if (collection.getModelClass().equals(modelClass))
                return (DbCollection<T>) collection;
        }
        throw new InvalidCollectionException(modelClass.getSimpleName());
    }

    /**
     * Gets a collection by table name
     * @param name
     * @return
     */
    public DbCollection getCollection(String name) {
        initialize();
        for (DbCollection collection: _collections) {
            if (collection.getTableName().equalsIgnoreCase(name))
                return collection;
        }
        throw new InvalidCollectionException(name);
    }

    /**
     * Implementers can override this to provide a list of entity names in the desired sync order.
     * @return
     */
    public String[] getSyncOrder() {
        String[] entityNames = new String[_collections.size()];
        for (int i=0; i<_collections.size(); i++)
            entityNames[i] = _collections.get(i).getTableName();
        return entityNames;
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
        SQLiteDatabase db = getWritableDatabase();
        for (DbCollection collection: _collections) {
            result.mergeFrom(collection.save(this, db));
        }
        return result;
    }

    //endregion


    //region Exceptions

    public static class InvalidCollectionException extends RuntimeException {
        public InvalidCollectionException(String collectionName) {
            super("Collection " + collectionName + " does not exist in this context");
        }
    }

    public static class InvalidAssociationException extends RuntimeException {
        public InvalidAssociationException(String rel, String name) {
            super("No " + rel + " association found for " + name);
        }
    }

    public static class InvalidRecordException extends RuntimeException {
        public InvalidRecordException(String collectionName, String message) {
            super("Record for " + collectionName + " " + message);
        }
    }

    //endregion


    //region JSON Serialization

    public final class DateTimeGsonSerializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>
    {

        @Override
        public DateTime deserialize(final JsonElement je, final Type type,
                                    final JsonDeserializationContext jdc) throws JsonParseException
        {
            long longValue = je.getAsLong();
            if (longValue == 0)
                return null;
            return new DateTime(longValue);
        }

        @Override
        public JsonElement serialize(final DateTime src, final Type typeOfSrc,
                                     final JsonSerializationContext context)
        {
            return new JsonPrimitive(src == null ? 0 : src.getMillis());
        }
    }

    public final class ObjectIdGsonSerializer implements JsonDeserializer<ObjectId>, JsonSerializer<ObjectId>
    {

        @Override
        public ObjectId deserialize(final JsonElement je, final Type type,
                                    final JsonDeserializationContext jdc) throws JsonParseException
        {
            String stringValue = je.getAsString();
            if (stringValue == null)
                return null;
            return new ObjectId(stringValue);
        }

        @Override
        public JsonElement serialize(final ObjectId src, final Type typeOfSrc,
                                     final JsonSerializationContext context)
        {
            return new JsonPrimitive(src == null ? null : src.toString());
        }
    }

    public final class PrivateExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().startsWith("_");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    /**
     * @return a Gson object that properly serializes the types used by TinySync
     */
    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeGsonSerializer())
                .registerTypeAdapter(ObjectId.class, new ObjectIdGsonSerializer())
                .addSerializationExclusionStrategy(new PrivateExclusionStrategy())
                .create();
    }

    //endregion
}
