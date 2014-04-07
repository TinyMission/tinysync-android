package com.tinymission.tinysync.db;

/**
 * Create a public field of this type on a model class to represent a belongs-to relationship to another model.
 */
public class DbBelongsTo<T extends DbModel> {
    public DbBelongsTo(DbModel record, Class<T> modelClass) {
        _modelClass = modelClass;
        _record = record;
    }

    private Class<T> _modelClass;

    public Class<T> getModelClass() {
        return _modelClass;
    }

    private DbModel _record;

    private ObjectId _key;

    /**
     * @return the key value used to look up the relationship
     */
    public ObjectId getKey() {
        return _key;
    }

    /**
     * Sets the key value used to look up the relationship
     * @param key
     */
    public void setKey(ObjectId key) {
        _key = key;
    }

    private T _cachedValue;

    /**
     * Gets the last value that was retrieved from the database (or assigned directly).
     * This could be null if the value hasn't been populated, so use getValue(DbContext)
     * if you're not sure there's a value there.
     * @return
     */
    public T getCachedValue() {
        return _cachedValue;
    }

    /**
     * Gets the value of the relationship (either cached or directly from the database).
     * @param context the context used to get the value if it isn't in the cache
     * @return
     */
    public T getValue(DbContext context) {
        if (_cachedValue != null)
            return _cachedValue;
        DbCollection<T> collection = context.getCollection(_modelClass);
        _cachedValue = collection.find(_key);
        return _cachedValue;
    }

    /**
     * Assigns the value of the relationship (and corresponding id).
     * @param value
     */
    public void setValue(T value) {
        _cachedValue = value;
        _key = value.id;
    }
}
