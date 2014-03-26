package com.tinymission.tinysync.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.CaseFormat;
import com.tinymission.tinysync.validation.RecordError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Provides an interface to query and persist records to a single table.
 */
public class DbSet<T extends DbModel> {
    private static final String LogTag = "tinysync.db.DbSet";

    public DbSet(Class<T> modelClass) {
        _tableName = tableizeClassName(modelClass.getSimpleName());
        Log.v(LogTag, "Parsing columns for table " + _tableName + " (" + modelClass.getName() + ")");

        for (Field field: modelClass.getFields()) {
            for (Annotation ann: field.getDeclaredAnnotations()) {
                if (ann instanceof DbColumn) {
                    DbColumnMap columnMap = new DbColumnMap((DbColumn)ann, field);
                    _columnMaps.put(columnMap.getColumnName(), columnMap);
                    Log.v(LogTag, "  " + field.getName() + " is a column named " + columnMap.getColumnName() + " of type " + field.getType());
                }
            }
        }
    }


    //region Schema

    private String _tableName;
    /**
     * @return the name of the table that this set maps to
     */
    public String getTableName() {
        return _tableName;
    }

    /**
     * Converts a class name into something that's appropriate to use for a table.
     * @param name a PascalCase class name
     * @return
     */
    public static String tableizeClassName(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    private HashMap<String,DbColumnMap> _columnMaps = new HashMap<String, DbColumnMap>();

    /**
     * @return the column definitions, for use with CREATE TABLE.
     */
    public List<String> getColumnDefs() {
        ArrayList<String> defs = new ArrayList<String>();
        for (DbColumnMap columnMap: _columnMaps.values()) {
            defs.add(columnMap.getColumnDef());
        }
        return defs;
    }

    //endregion


    //region Persistence

    private HashSet<T> _newRecords = new HashSet<T>();
    private HashSet<T> _changedRecords = new HashSet<T>();

    /**
     * Adds a new or existing record to be persisted when save() is called.
     */
    public void add(T record) {
        if (record.isPersisted())
            _changedRecords.add(record);
        else
            _newRecords.add(record);
    }

    private boolean insertRecord(SQLiteDatabase db, T record) {
        ContentValues values = new ContentValues(_columnMaps.size());
        for (DbColumnMap columnMap : _columnMaps.values()) {
            try {
                columnMap.assignContentValue(record, values);
            }
            catch (Exception ex) {
                record.addError(new RecordError(record, columnMap.getColumnName(), ex));
            }
        }
        try {
            db.insertOrThrow(_tableName, null, values);
        }
        catch (Exception ex) {
            record.addError(new RecordError(record, null, ex));
        }
        return !record.hasErrors();
    }

    /**
     * Saves all new and changed records to the database.
     * @return a SaveResult containing information about the operation
     */
    SaveResult save(DbContext context, SQLiteDatabase db) {
        SaveResult result = new SaveResult();
        for (T record: _newRecords) {
            if (insertRecord(db, record)) {
                record._persisted = true;
                result.addInserted(record);
            }
            else {
                result.addErrored(record);
            }
            record.logErrors();
        }

        // remove all successfully persisted records from the queues
        for (DbModel record: result.getInserted()) {
            if (_newRecords.contains(record))
                _newRecords.remove(record);
        }
        for (DbModel record: result.getUpdated()) {
            if (_changedRecords.contains(record))
                _changedRecords.remove(record);
        }

        return result;
    }

    //endregion
}
