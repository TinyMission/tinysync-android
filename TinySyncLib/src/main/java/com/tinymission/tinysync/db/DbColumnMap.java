package com.tinymission.tinysync.db;

import android.content.ContentValues;

import com.google.common.base.CaseFormat;
import com.tinymission.tinysync.serializers.DbSerializer;
import com.tinymission.tinysync.serializers.StringSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Stores meta information needed to map a model field to a database column.
 */
public class DbColumnMap {
    String _columnName;
    /**
     * @return the name of the corresponding database column.
     */
    public String getColumnName() {
        return _columnName;
    }

    /**
     * @return the type of the field.
     */
    public Class<?> getType() {
        return _field.getType();
    }

    Field _field;
    /**
     * @return the field this columns maps to.
     */
    public Field getField() {
        return _field;
    }

    DbSerializer _serializer;

    public DbColumnMap(DbColumn column, Field field) {
        // compute the column name
        if (column.name().length() > 0)
            _columnName = column.name();
        else
            _columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());

        _field = field;

        // determine the serializer to use
        _serializer = DbSerializer.factory(field.getType());
    }

    /**
     * @return a string that can be used to create this column.
     */
    public String getColumnDef() {
        return _columnName + " " + DbSerializer.columnTypeName(_serializer.getColumnType());
    }

    public void assignContentValue(DbModel record, ContentValues contentValues) throws IllegalAccessException {
        _serializer.serialize(record, _field, contentValues, _columnName);
    }

}
