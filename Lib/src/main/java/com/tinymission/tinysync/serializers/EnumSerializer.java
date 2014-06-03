package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;

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
        String valString = cursor.getString(columnIndex);
        if (valString == null)
            field.set(model, null);
        else
            field.set(model, Enum.valueOf(_enumClass, valString));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        Object value = field.get(model);
        if (value == null)
            return null;
        return value.toString();
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
