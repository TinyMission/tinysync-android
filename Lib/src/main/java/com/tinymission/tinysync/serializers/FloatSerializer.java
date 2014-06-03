package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;

import java.lang.reflect.Field;

/**
 * Serializes floats and doubles to and from the database.
 */
public class FloatSerializer extends DbSerializer {

    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        field.set(model, cursor.getFloat(columnIndex));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        Double value = (Double)field.get(model);
        return value.toString();
    }

    @Override
    public void serialize(DbModel model, Field field, ContentValues values, String name) throws IllegalAccessException {
        values.put(name, field.getFloat(model));
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_FLOAT;
    }
}
