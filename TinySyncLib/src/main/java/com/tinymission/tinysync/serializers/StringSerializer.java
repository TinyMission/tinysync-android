package com.tinymission.tinysync.serializers;

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
}
