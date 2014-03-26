package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;

import java.lang.reflect.Field;

/**
 * Serializes strings to and from the database.
 */
public class StringSerializer extends DbSerializer {
    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        field.set(model, cursor.getString(columnIndex));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return (String)field.get(model);
    }

    @Override
    public void serialize(DbModel model, Field field, ContentValues values, String name) throws IllegalAccessException {
        values.put(name, field.get(model).toString());
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_STRING;
    }
}
