package com.tinymission.tinysync.db;

import android.util.Log;

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

    public DbContext() {
    }


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

}
