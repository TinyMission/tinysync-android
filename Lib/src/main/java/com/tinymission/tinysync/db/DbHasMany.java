package com.tinymission.tinysync.db;

/**
 * Make a public field of this type on a model class to specify a has-many relationship to another model.
 */
public class DbHasMany<T extends DbModel> {

    public DbHasMany(DbModel record, Class<T> manyClass) {
        _manyClass = manyClass;
        _oneClass = (Class<DbModel>)record.getClass();
        _record = record;
    }

    private Class<T> _manyClass;
    private Class<DbModel> _oneClass;
    private DbModel _record;

    public Class<T> getModelClass() {
        return _manyClass;
    }

    private DbSet<T> _cachedValues;

    /**
     * Gets the previously retrieved values from the cache.
     * This will not touch the database, and will return null if the values
     * haven't been retrieved and this association wasn't included in the query.
     * @return
     */
    public DbSet<T> getCachedValues() {
        return _cachedValues;
    }

    public DbSet<T> getValues(DbContext context) {
        if (_cachedValues != null)
            return _cachedValues;
        DbCollection<T> manyCollection = context.getCollection(_manyClass);
        DbCollection<?> oneCollection = context.getCollection(_oneClass);
        DbHasManyMeta meta = oneCollection.getHasManyMeta(_manyClass);
        _cachedValues = manyCollection.where(meta.getForeignKey(), _record.id).run();
        return _cachedValues;
    }
}
