package com.tinymission.tinysync.serializers;

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
    public int getColumnType() {
        return Cursor.FIELD_TYPE_INTEGER;
    }
}
