package com.tinymission.tinysync.serializers;

import android.database.Cursor;

import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;

import java.lang.reflect.Field;

/**
 * Serializes object id values.
 */
public class ObjectIdSerializer extends DbSerializer {
    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        String stringValue = cursor.getString(columnIndex);
        field.set(model, new ObjectId(stringValue));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return field.get(model).toString();
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_STRING;
    }
}
