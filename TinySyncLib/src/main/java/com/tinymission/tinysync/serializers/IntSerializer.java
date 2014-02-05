package com.tinymission.tinysync.serializers;

import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;

import java.lang.reflect.Field;

/**
 * Serializes integer values.
 */
public class IntSerializer extends DbSerializer {
    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        field.set(model, cursor.getInt(columnIndex));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        Integer value = (Integer)field.get(model);
        return value.toString();
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_INTEGER;
    }
}
