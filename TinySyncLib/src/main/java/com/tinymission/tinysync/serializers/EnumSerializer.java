package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;

import java.lang.reflect.Field;

/**
 * Serializes enum values.
 */
public class EnumSerializer extends DbSerializer {

    Class<Enum> _enumClass;

    public EnumSerializer(Class<Enum> enumClass) {
        _enumClass = enumClass;
    }

    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        Enum.valueOf(_enumClass, cursor.getString(columnIndex));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return field.get(model).toString();
    }

    @Override
    public void serialize(DbModel model, Field field, ContentValues values, String name) throws IllegalAccessException {
        Object value = field.get(model);
        if (value != null)
            values.put(name, value.toString());
        else
            values.putNull(name);
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_STRING;
    }
}
