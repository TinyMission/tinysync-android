package com.tinymission.tinysync.db;

import android.util.Log;

import com.google.common.base.CaseFormat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Provides an interface to query and persist records to a single table.
 */
public class DbSet<T extends DbModel> {
    private static final String LogTag = "tinysync.db.DbSet";

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



}
