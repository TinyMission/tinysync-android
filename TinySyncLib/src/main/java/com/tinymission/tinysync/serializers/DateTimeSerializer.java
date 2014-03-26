package com.tinymission.tinysync.serializers;

import android.content.ContentValues;
import android.database.Cursor;
import com.tinymission.tinysync.db.DbModel;
import org.joda.time.DateTime;
import java.lang.reflect.Field;

/**
 * Serializes Joda DateTime values.
 */
public class DateTimeSerializer extends DbSerializer {

    private String valueToString(DbModel model, Field field) throws IllegalAccessException {
        DateTime dt = (DateTime)field.get(model);
        if (dt == null)
            return null;
        return dt.toString();
    }

    @Override
    public void deserializeColumn(Cursor cursor, DbModel model, int columnIndex, Field field) throws IllegalAccessException {
        String stringValue = cursor.getString(columnIndex);
        if (stringValue == null)
            field.set(model, null);
        else
            field.set(model, DateTime.parse(stringValue));
    }

    @Override
    public String serialize(DbModel model, Field field) throws IllegalAccessException {
        return valueToString(model, field);
    }

    @Override
    public void serialize(DbModel model, Field field, ContentValues values, String name) throws IllegalAccessException {
        values.put(name, valueToString(model, field));
    }

    @Override
    public int getColumnType() {
        return Cursor.FIELD_TYPE_STRING;
    }
}
